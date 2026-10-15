package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.catalogo.Cliente;
import pe.altoque.common.NumeroALetras;
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

/**
 * Boleta, factura o nota. Tipo, serie y local salen de la serie; lo que se puede calcular
 * (monto en letras, nombre de archivo, lo cobrado) no se guarda.
 */
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
        /** SUNAT lo rechazó (2000–3999): nunca tuvo validez y su número no se reutiliza. */
        RECHAZADO,
        /** Baja o resumen de anulación enviado, esperando ticket. */
        POR_ANULAR,
        ANULADO
    }

    public enum EstadoPago { PAGADO, PARCIAL, POR_COBRAR }

    public enum FormaPago { CONTADO, CREDITO }

    /** Catálogo 51. */
    public static final String OPERACION_VENTA_INTERNA = "0101";
    public static final String OPERACION_DETRACCION = "1001";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    /** Siempre se carga: de aquí salen el tipo, la serie y el local. */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "serie_id")
    private Serie serieRef;

    private int correlativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    private LocalDate fechaEmision;
    private LocalTime horaEmision;
    private LocalDate fechaVencimiento;
    private String moneda = "PEN";
    private String tipoOperacion = OPERACION_VENTA_INTERNA;

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
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private EstadoSunat estadoSunat = EstadoSunat.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PAGADO;

    private String hash;
    private String tokenPublico;
    private Instant createdAt;
    private Instant updatedAt;

    @OneToOne(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ComprobanteNota nota;

    @OneToOne(mappedBy = "comprobante", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ComprobanteDetraccion detraccion;

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

    // ------------------------------------------------------------------ derivados

    public TipoComprobante getTipo() {
        return serieRef.getTipoComprobante();
    }

    public String getSerie() {
        return serieRef.getSerie();
    }

    public Establecimiento getEstablecimiento() {
        return serieRef.getEstablecimiento();
    }

    public String getTotalLetras() {
        return NumeroALetras.convertir(total, moneda);
    }

    /** Nombre de los archivos ante SUNAT: RUC-TIPO-SERIE-CORRELATIVO. */
    public String getNombreArchivo() {
        return "%s-%s-%s-%d".formatted(empresa.getRuc(), getTipo().codigo(), getSerie(), correlativo);
    }

    public BigDecimal getMontoPagado() {
        return pagos.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Lo que el cliente le paga al emisor: el total menos la detracción que deposita en el Banco de la Nación. */
    public BigDecimal getNetoACobrar() {
        return detraccion == null ? total : total.subtract(detraccion.getMonto());
    }

    public BigDecimal getSaldo() {
        return getNetoACobrar().subtract(getMontoPagado());
    }

    // Datos de la nota (null si no es nota)

    public Comprobante getComprobanteRef() {
        return nota == null ? null : nota.getComprobanteRef();
    }

    public TipoComprobante getRefTipo() {
        return nota == null ? null : nota.getRefTipo();
    }

    public String getRefSerie() {
        return nota == null ? null : nota.getRefSerie();
    }

    public Integer getRefCorrelativo() {
        return nota == null ? null : nota.getRefCorrelativo();
    }

    public String getMotivoCodigo() {
        return nota == null ? null : nota.getMotivoCodigo();
    }

    public String getMotivoDescripcion() {
        return nota == null ? null : nota.getSustento();
    }

    // ------------------------------------------------------------------ armado

    public String numero() {
        return getSerie() + "-" + correlativo;
    }

    /** Número con ceros a la izquierda como se imprime: B001-000123. */
    public String numeroImpreso() {
        return "%s-%06d".formatted(getSerie(), correlativo);
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

    public void asignarNota(ComprobanteNota n) {
        n.setComprobante(this);
        n.setEmpresaId(empresa.getId());
        nota = n;
    }

    public void asignarDetraccion(ComprobanteDetraccion d) {
        d.setComprobante(this);
        detraccion = d;
        tipoOperacion = OPERACION_DETRACCION;
    }

    /** Boletas y sus notas se anulan por resumen diario; facturas y sus notas por comunicación de baja. */
    public boolean seAnulaPorResumen() {
        return getSerie().startsWith("B");
    }

    public boolean vigente() {
        return estadoSunat == EstadoSunat.ACEPTADO || estadoSunat == EstadoSunat.OBSERVADO;
    }
}
