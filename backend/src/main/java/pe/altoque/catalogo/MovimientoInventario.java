package pe.altoque.catalogo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Una línea del kardex: cuánto entró o salió de un producto, por qué y con qué saldo quedó. */
@Entity
@Table(name = "movimiento_inventario")
@Getter
@Setter
public class MovimientoInventario {

    public enum Tipo {
        /** Salida por boleta o factura. */
        VENTA,
        /** Entrada por nota de crédito (devolución o anulación de la operación). */
        DEVOLUCION,
        /** Entrada porque el comprobante se anuló o SUNAT lo rechazó. */
        ANULACION,
        /** Corrección manual del conteo. */
        AJUSTE,
        /** Compra o reposición. */
        INGRESO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empresaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private BigDecimal cantidad;
    private BigDecimal stockResultante;
    private Long comprobanteId;
    private Long usuarioId;
    private String nota;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
