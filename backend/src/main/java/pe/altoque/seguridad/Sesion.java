package pe.altoque.seguridad;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.altoque.common.ApiException;
import pe.altoque.usuario.Usuario.Rol;

/** Quién está usando la API (sale del JWT). Todas las consultas se filtran por empresaId. */
public record Sesion(Long usuarioId, Long empresaId, Long establecimientoId, Rol rol, String email) {

    public static Sesion actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Sesion s)) {
            throw new ApiException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Inicia sesión para continuar.");
        }
        return s;
    }

    public boolean esAdmin() {
        return rol == Rol.PROPIETARIO || rol == Rol.ADMINISTRADOR;
    }
}
