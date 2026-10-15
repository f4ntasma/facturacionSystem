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

/**
 * Equipo del negocio. El id que viaja es el del miembro (la persona dentro de este negocio).
 * Si el correo ya tiene cuenta en Al Toque, se le da acceso a este negocio sin tocar su contraseña.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasAnyRole('PROPIETARIO','ADMINISTRADOR')")
public class UsuarioController {

    public record UsuarioVista(Long id, String nombre, String email, Usuario.Rol rol, boolean activo, Long establecimientoId,
                               String establecimiento, Instant ultimoAcceso) {
        static UsuarioVista de(Miembro m) {
            Usuario u = m.getUsuario();
            return new UsuarioVista(m.getId(), u.getNombre(), u.getEmail(), m.getRol(), m.isActivo(),
                    m.getEstablecimiento() != null ? m.getEstablecimiento().getId() : null,
                    m.getEstablecimiento() != null ? m.getEstablecimiento().getNombre() : null, u.getUltimoAcceso());
        }
    }

    public record UsuarioRequest(@NotBlank(message = "Escribe el nombre") @Size(max = 120) String nombre,
                                 @NotBlank(message = "Escribe el correo") @Email(message = "Correo inválido") String email,
                                 @NotNull Usuario.Rol rol,
                                 Long establecimientoId,
                                 Boolean activo,
                                 @Size(min = 8, max = 72, message = "La contraseña debe tener al menos 8 caracteres") String password) {}

    private final UsuarioRepository usuarios;
    private final MiembroRepository miembros;
    private final EmpresaRepository empresas;
    private final EstablecimientoRepository locales;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository usuarios, MiembroRepository miembros, EmpresaRepository empresas,
                             EstablecimientoRepository locales, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.miembros = miembros;
        this.empresas = empresas;
        this.locales = locales;
        this.encoder = encoder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UsuarioVista> listar() {
        return miembros.equipo(Sesion.actual().empresaId()).stream().map(UsuarioVista::de).toList();
    }

    @PostMapping
    @Transactional
    public UsuarioVista crear(@Valid @RequestBody UsuarioRequest r) {
        Sesion s = Sesion.actual();
        var empresa = empresas.findWithPlanById(s.empresaId()).orElseThrow();
        if (miembros.countByEmpresaIdAndActivoTrue(s.empresaId()) >= empresa.getPlan().getMaxUsuarios()) {
            throw ApiException.invalido("Tu plan permite %d usuario(s). Sube de plan para sumar más gente.".formatted(empresa.getPlan().getMaxUsuarios()));
        }
        validarRol(s, r.rol());
        String email = r.email().trim().toLowerCase(Locale.ROOT);
        Usuario u = usuarios.findByEmailIgnoreCase(email).orElse(null);
        if (u == null) {
            if (r.password() == null) throw ApiException.invalido("Ponle una contraseña inicial (mínimo 8 caracteres).");
            u = new Usuario();
            u.setNombre(r.nombre().trim());
            u.setEmail(email);
            u.setPasswordHash(encoder.encode(r.password()));
            usuarios.save(u);
        } else if (miembros.findByUsuarioIdAndEmpresaId(u.getId(), s.empresaId()).isPresent()) {
            throw ApiException.conflicto("Esa persona ya es parte de tu equipo.");
        }
        Miembro m = new Miembro();
        m.setUsuario(u);
        m.setEmpresa(empresa);
        m.setRol(r.rol());
        m.setEstablecimiento(local(s, r.establecimientoId()));
        return UsuarioVista.de(miembros.save(m));
    }

    @PutMapping("/{id}")
    @Transactional
    public UsuarioVista actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest r) {
        Sesion s = Sesion.actual();
        Miembro m = miembros.findByIdAndEmpresaId(id, s.empresaId()).orElseThrow(() -> ApiException.noEncontrado("Usuario"));
        if (m.getRol() == Usuario.Rol.PROPIETARIO && (r.rol() != Usuario.Rol.PROPIETARIO || Boolean.FALSE.equals(r.activo()))) {
            throw ApiException.invalido("El dueño del negocio no se puede desactivar ni cambiar de rol.");
        }
        if (m.getRol() != Usuario.Rol.PROPIETARIO) validarRol(s, r.rol());
        m.setRol(r.rol());
        if (r.activo() != null) m.setActivo(r.activo());
        if (r.establecimientoId() != null) m.setEstablecimiento(local(s, r.establecimientoId()));
        // Nombre, correo y contraseña son de la persona: solo se cambian si pertenece únicamente a este negocio.
        Usuario u = m.getUsuario();
        boolean soloAqui = miembros.activosDe(u.getId()).stream().allMatch(x -> x.getEmpresa().getId().equals(s.empresaId()));
        if (soloAqui) {
            String email = r.email().trim().toLowerCase(Locale.ROOT);
            if (!u.getEmail().equalsIgnoreCase(email) && usuarios.existsByEmailIgnoreCase(email)) {
                throw ApiException.conflicto("Ese correo ya está registrado.");
            }
            u.setNombre(r.nombre().trim());
            u.setEmail(email);
            if (r.password() != null && !r.password().isBlank()) u.setPasswordHash(encoder.encode(r.password()));
        }
        return UsuarioVista.de(m);
    }

    private Establecimiento local(Sesion s, Long establecimientoId) {
        if (establecimientoId != null) {
            return locales.findByIdAndEmpresaId(establecimientoId, s.empresaId()).orElseThrow(() -> ApiException.noEncontrado("Local"));
        }
        return locales.findByEmpresaIdAndCodigoSunat(s.empresaId(), "0000").orElse(null);
    }

    private void validarRol(Sesion s, Usuario.Rol rol) {
        if (rol == Usuario.Rol.PROPIETARIO) throw ApiException.invalido("Solo puede haber un dueño por negocio.");
        if (rol == Usuario.Rol.ADMINISTRADOR && s.rol() != Usuario.Rol.PROPIETARIO) {
            throw ApiException.prohibido("Solo el dueño puede crear administradores.");
        }
    }
}
