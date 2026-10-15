package pe.altoque.usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.empresa.EmpresaService;
import pe.altoque.empresa.EmpresaService.DatosEmpresa;
import pe.altoque.empresa.EmpresaService.DatosPersona;
import pe.altoque.seguridad.JwtService;
import pe.altoque.seguridad.Sesion;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(@NotBlank(message = "Escribe tu correo") String email,
                               @NotBlank(message = "Escribe tu contraseña") String password) {}

    public record NegocioRequest(
            @NotBlank(message = "Escribe el RUC") @Pattern(regexp = "\\d{11}", message = "El RUC tiene 11 dígitos") String ruc,
            @NotBlank(message = "Escribe la razón social") @Size(max = 200) String razonSocial,
            @Size(max = 200) String nombreComercial,
            @Size(max = 250) String direccion,
            String ubigeo, String departamento, String provincia, String distrito,
            @Size(max = 20) String telefono,
            String plan) {

        DatosEmpresa datos() {
            return new DatosEmpresa(ruc, razonSocial, nombreComercial, direccion, ubigeo, departamento, provincia, distrito, telefono, plan);
        }
    }

    public record RegistroRequest(
            @NotBlank(message = "Escribe el RUC") @Pattern(regexp = "\\d{11}", message = "El RUC tiene 11 dígitos") String ruc,
            @NotBlank(message = "Escribe la razón social") @Size(max = 200) String razonSocial,
            @Size(max = 200) String nombreComercial,
            @Size(max = 250) String direccion,
            String ubigeo, String departamento, String provincia, String distrito,
            @Size(max = 20) String telefono,
            @NotBlank(message = "Escribe tu nombre") @Size(max = 120) String nombre,
            @NotBlank(message = "Escribe tu correo") @Email(message = "Ese correo no es válido") String email,
            @NotBlank @Size(min = 8, max = 72, message = "La contraseña debe tener al menos 8 caracteres") String password,
            String plan) {}

    public record CambioClaveRequest(@NotBlank String actual,
                                     @NotBlank @Size(min = 8, max = 72, message = "La nueva contraseña debe tener al menos 8 caracteres") String nueva) {}

    public record NegocioVista(Long empresaId, String ruc, String nombre, Usuario.Rol rol, boolean actual) {}

    public record TokenResponse(String token, Instant expira, PerfilService.Perfil perfil) {}

    private final UsuarioRepository usuarios;
    private final MiembroRepository miembros;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmpresaService empresaService;
    private final PerfilService perfiles;

    public AuthController(UsuarioRepository usuarios, MiembroRepository miembros, PasswordEncoder encoder, JwtService jwt,
                          EmpresaService empresaService, PerfilService perfiles) {
        this.usuarios = usuarios;
        this.miembros = miembros;
        this.encoder = encoder;
        this.jwt = jwt;
        this.empresaService = empresaService;
        this.perfiles = perfiles;
    }

    @PostMapping("/login")
    @Transactional
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim()).orElseThrow(() -> new BadCredentialsException("x"));
        if (!encoder.matches(req.password(), u.getPasswordHash())) throw new BadCredentialsException("x");
        if (!u.isActivo()) throw new DisabledException("x");
        List<Miembro> negocios = miembros.activosDe(u.getId());
        if (negocios.isEmpty()) throw new DisabledException("x");
        u.setUltimoAcceso(Instant.now());
        return token(negocios.getFirst());
    }

    @PostMapping("/registro")
    public TokenResponse registro(@Valid @RequestBody RegistroRequest r) {
        Miembro m = empresaService.registrar(
                new DatosEmpresa(r.ruc(), r.razonSocial(), r.nombreComercial(), r.direccion(), r.ubigeo(), r.departamento(),
                        r.provincia(), r.distrito(), r.telefono(), r.plan()),
                new DatosPersona(r.nombre(), r.email(), r.password()));
        return token(miembros.findByUsuarioIdAndEmpresaId(m.getUsuario().getId(), m.getEmpresa().getId()).orElseThrow());
    }

    @GetMapping("/yo")
    public PerfilService.Perfil yo() {
        Sesion s = Sesion.actual();
        return perfiles.de(s.usuarioId(), s.empresaId());
    }

    /** Negocios a los que puede entrar la persona (para el selector de negocio). */
    @GetMapping("/negocios")
    @Transactional(readOnly = true)
    public List<NegocioVista> negocios() {
        Sesion s = Sesion.actual();
        return miembros.activosDe(s.usuarioId()).stream()
                .map(m -> new NegocioVista(m.getEmpresa().getId(), m.getEmpresa().getRuc(), m.getEmpresa().nombreVisible(), m.getRol(),
                        m.getEmpresa().getId().equals(s.empresaId())))
                .toList();
    }

    @PostMapping("/negocios/{empresaId}/entrar")
    @Transactional(readOnly = true)
    public TokenResponse entrar(@PathVariable Long empresaId) {
        Miembro m = miembros.findByUsuarioIdAndEmpresaId(Sesion.actual().usuarioId(), empresaId)
                .filter(Miembro::isActivo)
                .orElseThrow(() -> ApiException.prohibido("No tienes acceso a ese negocio."));
        return token(m);
    }

    @PostMapping("/negocios")
    public TokenResponse agregarNegocio(@Valid @RequestBody NegocioRequest r) {
        Miembro m = empresaService.agregarNegocio(Sesion.actual().usuarioId(), r.datos());
        return token(miembros.findByUsuarioIdAndEmpresaId(m.getUsuario().getId(), m.getEmpresa().getId()).orElseThrow());
    }

    @PostMapping("/clave")
    @Transactional
    public void cambiarClave(@Valid @RequestBody CambioClaveRequest req) {
        Usuario u = usuarios.findById(Sesion.actual().usuarioId()).orElseThrow();
        if (!encoder.matches(req.actual(), u.getPasswordHash())) throw ApiException.invalido("Tu contraseña actual no coincide.");
        u.setPasswordHash(encoder.encode(req.nueva()));
    }

    private TokenResponse token(Miembro m) {
        String t = jwt.generar(m);
        return new TokenResponse(t, jwt.expiraEn(t), perfiles.de(m.getUsuario().getId(), m.getEmpresa().getId()));
    }
}
