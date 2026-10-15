package pe.altoque.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.Establecimiento;

import java.time.Instant;

@Entity
@Table(name = "usuario")
@Getter
@Setter
public class Usuario {

    public enum Rol {
        /** Dueño del negocio: todo, incluido plan y SUNAT. */
        PROPIETARIO,
        /** Gestiona catálogo, usuarios y anulaciones. */
        ADMINISTRADOR,
        /** Emite y cobra. */
        VENDEDOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;

    private String nombre;
    private String email;
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Rol rol;

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
