package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.catalogo.Producto;

import java.math.BigDecimal;

@Entity
@Table(name = "comprobante_detalle")
@Getter
@Setter
public class ComprobanteDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private int item;
    private String codigo;
    private String descripcion;
    private String unidadMedida;
    private BigDecimal cantidad;
    /** Con IGV. */
    private BigDecimal precioUnitario;
    /** Sin IGV, con hasta 10 decimales. */
    private BigDecimal valorUnitario;
    private String tipoAfectacion;
    private BigDecimal valorVenta;
    private BigDecimal igv;
    private BigDecimal icbper = BigDecimal.ZERO;

    /** Importe de la línea con impuestos (no se guarda: es valor de venta + IGV + ICBPER). */
    public BigDecimal getTotal() {
        return valorVenta.add(igv).add(icbper);
    }
}
