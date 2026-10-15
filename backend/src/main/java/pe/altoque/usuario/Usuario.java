package pe.altoque.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Una persona con su correo y contraseña. Lo que puede hacer en cada negocio está en {@link Miembro}. */
@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    public enum Rol {
        /** Dueño del negocio: todo, incluido plan y SUNAT. */
        PROPIETARIO,
        /** Gestiona catálogo, equipo y anulaciones. */
        ADMINISTRADOR,
        /** Emite y cobra. */
        VENDEDOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String email;
    private String passwordHash;
    private boolean activo = true;
    private Instant ultimoAcceso;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void alCrear() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }
}
