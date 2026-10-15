package pe.altoque.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import pe.altoque.config.AltoqueProperties;
import pe.altoque.usuario.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration vigencia;

    public JwtService(AltoqueProperties props) {
        if (props.jwtSecret() == null || props.jwtSecret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
        }
        this.clave = Keys.hmacShaKeyFor(props.jwtSecret().getBytes(StandardCharsets.UTF_8));
        this.vigencia = Duration.ofHours(props.jwtHoras());
    }

    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(u.getEmail())
                .claim("uid", u.getId())
                .claim("eid", u.getEmpresa().getId())
                .claim("lid", u.getEstablecimiento() != null ? u.getEstablecimiento().getId() : null)
                .claim("rol", u.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(vigencia)))
                .signWith(clave)
                .compact();
    }

    public Instant expiraEn(String token) {
        return leer(token).getExpiration().toInstant();
    }

    /** Lanza excepción si el token es inválido o venció. */
    public Sesion validar(String token) {
        Claims c = leer(token);
        Number lid = c.get("lid", Number.class);
        return new Sesion(
                c.get("uid", Number.class).longValue(),
                c.get("eid", Number.class).longValue(),
                lid != null ? lid.longValue() : null,
                Usuario.Rol.valueOf(c.get("rol", String.class)),
                c.getSubject());
    }

    private Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
