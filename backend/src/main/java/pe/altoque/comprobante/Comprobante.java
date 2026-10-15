package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.catalogo.Cliente;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.Establecimiento;
import pe.altoque.empresa.Serie;
import pe.altoque.usuario.Usuario;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comprobante")
@Getter
@Setter
public class Comprobante {

    public enum EstadoSunat {
        /** Generado y firmado; falta la respuesta de SUNAT (se reintenta solo). */
        PENDIENTE,
        ACEPTADO,
        /** Aceptado con observaciones (códigos 4000+). */
        OBSERVADO,
        RECHAZADO,
        /** Baja o resumen de anulación enviado, esperando ticket. */
        POR_ANULAR,
        ANULADO
    }

    public enum EstadoPago { PAGADO, PARCIAL, POR_COBRAR }

    public enum FormaPago { CONTADO, CREDITO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "serie_id")
    private Serie serieRef;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @Column(name = "tipo_comprobante")
    private TipoComprobante tipo;

    private String serie;
    private int correlativo;
    private LocalDate fechaEmision;
    private LocalTime horaEmision;
    private LocalDate fechaVencimiento;
    private String moneda = "PEN";
    private String tipoOperacion = "0101";

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago = FormaPago.CONTADO;

    private String clienteTipoDoc;
    private String clienteNumDoc;
    private String clienteNombre;
    private String clienteDireccion;
    private String clienteEmail;
    private String clienteTelefono;

    private BigDecimal igvPorcentaje;
    private BigDecimal totalGravado = BigDecimal.ZERO;
    private BigDecimal totalExonerado = BigDecimal.ZERO;
    private BigDecimal totalInafecto = BigDecimal.ZERO;
    private BigDecimal totalIgv = BigDecimal.ZERO;
    private BigDecimal totalIcbper = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private String totalLetras;
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_ref_id")
    private Comprobante comprobanteRef;

    private TipoComprobante refTipo;
    private String refSerie;
    private Integer refCorrelativo;
    private String motivoCodigo;
    private String motivoDescripcion;

    @Enumerated(EnumType.STRING)
    private EstadoSunat estadoSunat = EstadoSunat.PENDIENTE;

    private String sunatCodigo;
    private String sunatMensaje;
    private int sunatIntentos;
    private Instant enviadoAt;
    private String hash;
    private String nombreArchivo;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PAGADO;

    private BigDecimal montoPagado = BigDecimal.ZERO;
    private String anulacionMotivo;
    private Instant anuladoAt;
    private String tokenPublico;
    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("item")
    private List<ComprobanteDetalle> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("numero")
    private List<ComprobanteCuota> cuotas = new ArrayList<>();

    @OneToMany(mappedBy = "comprobante", cascade = CascadeType.ALL)
    @OrderBy("pagadoAt")
    private List<Pago> pagos = new ArrayList<>();

    @PrePersist
    void alCrear() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }

    public String numero() {
        return serie + "-" + correlativo;
    }

    /** Número con ceros a la izquierda como se imprime: B001-000123. */
    public String numeroImpreso() {
        return "%s-%06d".formatted(serie, correlativo);
    }

    public void agregarDetalle(ComprobanteDetalle d) {
        d.setComprobante(this);
        detalles.add(d);
    }

    public void agregarCuota(ComprobanteCuota c) {
        c.setComprobante(this);
        cuotas.add(c);
    }

    public void agregarPago(Pago p) {
        p.setComprobante(this);
        pagos.add(p);
    }

    /** Boletas y sus notas se anulan por resumen diario; facturas y sus notas por comunicación de baja. */
    public boolean seAnulaPorResumen() {
        return serie.startsWith("B");
    }
}
