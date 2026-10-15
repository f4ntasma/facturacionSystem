package pe.altoque.usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.Establecimiento;

import java.time.Instant;

/** Pertenencia de una persona a un negocio, con su rol y su local. Un contador puede tener varias. */
@Entity
@Table(name = "miembro")
@Getter
@Setter
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;

    @Enumerated(EnumType.STRING)
    private Usuario.Rol rol;

    private boolean activo = true;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
