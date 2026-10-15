package pe.altoque.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;

import java.time.Instant;

/** Caserito guardado. tipoDocumento según catálogo 06: 0 sin doc, 1 DNI, 4 CE, 6 RUC, 7 pasaporte. */
@Entity
@Table(name = "cliente")
@Getter
@Setter
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String tipoDocumento;
    private String numeroDocumento;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;
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
