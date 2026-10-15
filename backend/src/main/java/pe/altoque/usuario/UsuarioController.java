package pe.altoque.usuario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.empresa.EmpresaRepository;
import pe.altoque.empresa.Establecimiento;
import pe.altoque.empresa.EstablecimientoRepository;
import pe.altoque.seguridad.Sesion;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

/** Equipo del negocio: cajeros y administradores. */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAnyRole('PROPIETARIO','ADMINISTRADOR')")
public class UsuarioController {

    public record UsuarioVista(Long id, String nombre, String email, Usuario.Rol rol, boolean activo, Long establecimientoId,
                               String establecimiento, Instant ultimoAcceso) {
        static UsuarioVista de(Usuario u) {
            return new UsuarioVista(u.getId(), u.getNombre(), u.getEmail(), u.getRol(), u.isActivo(),
                    u.getEstablecimiento() != null ? u.getEstablecimiento().getId() : null,
                    u.getEstablecimiento() != null ? u.getEstablecimiento().getNombre() : null, u.getUltimoAcceso());
        }
    }

    public record UsuarioRequest(@NotBlank(message = "Escribe el nombre") @Size(max = 120) String nombre,
                                 @NotBlank(message = "Escribe el correo") @Email(message = "Correo inválido") String email,
                                 @NotNull Usuario.Rol rol,
                                 Long establecimientoId,
                                 Boolean activo,
                                 @Size(min = 8, max = 72, message = "La contraseña debe tener al menos 8 caracteres") String password) {}

    private final UsuarioRepository usuarios;
    private final EmpresaRepository empresas;
    private final EstablecimientoRepository locales;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository usuarios, EmpresaRepository empresas, EstablecimientoRepository locales, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.empresas = empresas;
        this.locales = locales;
        this.encoder = encoder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UsuarioVista> listar() {
        return usuarios.findByEmpresaIdOrderByNombre(Sesion.actual().empresaId()).stream().map(UsuarioVista::de).toList();
    }

    @PostMapping
    @Transactional
    public UsuarioVista crear(@Valid @RequestBody UsuarioRequest r) {
        Sesion s = Sesion.actual();
        var empresa = empresas.findWithPlanById(s.empresaId()).orElseThrow();
        if (usuarios.countByEmpresaIdAndActivoTrue(s.empresaId()) >= empresa.getPlan().getMaxUsuarios()) {
            throw ApiException.invalido("Tu plan permite %d usuario(s). Sube de plan para sumar más gente.".formatted(empresa.getPlan().getMaxUsuarios()));
        }
        if (r.password() == null) throw ApiException.invalido("Ponle una contraseña inicial (mínimo 8 caracteres).");
        if (usuarios.existsByEmailIgnoreCase(r.email().trim())) throw ApiException.conflicto("Ese correo ya está registrado.");
        validarRol(s, r.rol());
        Usuario u = new Usuario();
        u.setEmpresa(empresa);
        aplicar(u, r, s);
        u.setPasswordHash(encoder.encode(r.password()));
        return UsuarioVista.de(usuarios.save(u));
    }

    @PutMapping("/{id}")
    @Transactional
    public UsuarioVista actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        Sesion s = Sesion.actual();
        Usuario u = usuarios.findByIdAndEmpresaId(id, s.empresaId()).orElseThrow(() -> ApiException.noEncontrado("Usuario"));
        if (u.getRol() == Usuario.Rol.PROPIETARIO && (r.rol() != Usuario.Rol.PROPIETARIO || Boolean.FALSE.equals(r.activo()))) {
            throw ApiException.invalido("El dueño del negocio no se puede desactivar ni cambiar de rol.");
        }
        if (!u.getEmail().equalsIgnoreCase(r.email().trim()) && usuarios.existsByEmailIgnoreCase(r.email().trim())) {
            throw ApiException.conflicto("Ese correo ya está registrado.");
        }
        if (u.getRol() != Usuario.Rol.PROPIETARIO) validarRol(s, r.rol());
        aplicar(u, r, s);
        if (r.password() != null && !r.password().isBlank()) u.setPasswordHash(encoder.encode(r.password()));
        return UsuarioVista.de(u);
    }

    private void aplicar(Usuario u, UsuarioRequest r, Sesion s) {
        u.setNombre(r.nombre().trim());
        u.setEmail(r.email().trim().toLowerCase(Locale.ROOT));
        u.setRol(r.rol());
        if (r.activo() != null) u.setActivo(r.activo());
        if (r.establecimientoId() != null) {
            Establecimiento l = locales.findByIdAndEmpresaId(r.establecimientoId(), s.empresaId())
                    .orElseThrow(() -> ApiException.noEncontrado("Local"));
            u.setEstablecimiento(l);
        } else if (u.getEstablecimiento() == null) {
            u.setEstablecimiento(locales.findByEmpresaIdAndCodigoSunat(s.empresaId(), "0000").orElse(null));
        }
    }

    private void validarRol(Sesion s, Usuario.Rol rol) {
        if (rol == Usuario.Rol.PROPIETARIO) throw ApiException.invalido("Solo puede haber un dueño por negocio.");
        if (rol == Usuario.Rol.ADMINISTRADOR && s.rol() != Usuario.Rol.PROPIETARIO) {
            throw ApiException.prohibido("Solo el dueño puede crear administradores.");
        }
    }
}
