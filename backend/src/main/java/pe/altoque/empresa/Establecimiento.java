package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Local o sede; 0000 es el domicilio fiscal. */
@Entity
@Table(name = "establecimiento")
@Getter
@Setter
public class Establecimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String codigoSunat;
    private String nombre;
    private String direccion;
    private String ubigeo;
    private boolean activo = true;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
