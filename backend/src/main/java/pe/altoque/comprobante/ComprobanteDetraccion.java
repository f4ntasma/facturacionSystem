package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Detracción (SPOT) de una factura: el cliente deposita este monto en la cuenta del Banco de la Nación del emisor. */
@Entity
@Table(name = "comprobante_detraccion")
@Getter
@Setter
public class ComprobanteDetraccion {

    @Id
    private Long comprobanteId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    /** Catálogo 54. */
    private String bienCodigo;
    private BigDecimal porcentaje;
    private BigDecimal monto;
    private String cuentaBanco;
    /** Catálogo 59: 001 depósito en cuenta. */
    private String medioPago = "001";
}
