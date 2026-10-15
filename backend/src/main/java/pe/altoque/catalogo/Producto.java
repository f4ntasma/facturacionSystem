package pe.altoque.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "producto")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String codigo;
    private String nombre;
    private String descripcion;
    private String unidadMedida = "NIU";
    /** Precio de venta con IGV incluido. */
    private BigDecimal precio;
    /** Catálogo 07: 10 gravado, 20 exonerado, 30 inafecto. */
    private String tipoAfectacion = "10";
    private boolean aplicaIcbper;
    private boolean controlaStock;
    private BigDecimal stock = BigDecimal.ZERO;
    private boolean favorito;
    private boolean activo = true;
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
