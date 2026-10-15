package pe.altoque.comprobante;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import pe.altoque.comprobante.Comprobante.EstadoPago;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.comprobante.Comprobante.FormaPago;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class ComprobanteDtos {

    private ComprobanteDtos() {}

    // ------------------------------------------------------------------ entrada

    public record ClienteIn(
            Long id,
            @Pattern(regexp = "[01467A]", message = "Tipo de documento inválido") String tipoDocumento,
            @Size(max = 15) String numeroDocumento,
            @Size(max = 200) String nombre,
            @Size(max = 250) String direccion,
            @Email(message = "Correo inválido") @Size(max = 150) String email,
            @Size(max = 20) String telefono,
            boolean guardar) {}

    public record ItemIn(
            Long productoId,
            @Size(max = 30) String codigo,
            @NotBlank(message = "Cada ítem necesita una descripción") @Size(max = 500) String descripcion,
            @Size(max = 3) String unidadMedida,
            @NotNull @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero") BigDecimal cantidad,
            @NotNull @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero") BigDecimal precioUnitario,
            @Pattern(regexp = "10|20|30", message = "Afectación IGV inválida") String tipoAfectacion,
            Boolean icbper) {}

    public record PagoIn(@NotNull Pago.Metodo metodo,
                         @NotNull @DecimalMin(value = "0.01", message = "El pago debe ser mayor a cero") BigDecimal monto,
                         @Size(max = 80) String referencia) {}

    public record CuotaIn(@NotNull @DecimalMin("0.01") BigDecimal monto, @NotNull LocalDate fechaVencimiento) {}

    /** Detracción: código del catálogo 54 (el porcentaje sale del catálogo). */
    public record DetraccionIn(@NotBlank(message = "Elige el bien o servicio sujeto a detracción") String codigo) {}

    public record EmitirRequest(
            @NotNull(message = "Elige boleta o factura") TipoComprobante tipo,
            Long serieId,
            @Valid ClienteIn cliente,
            @NotEmpty(message = "Agrega al menos un producto") @Valid List<ItemIn> items,
            FormaPago formaPago,
            @Valid List<CuotaIn> cuotas,
            @Valid List<PagoIn> pagos,
            LocalDate fechaVencimiento,
            @Valid DetraccionIn detraccion,
            @Size(max = 500) String observaciones) {}

    public record NotaRequest(
            @NotNull TipoComprobante tipo,
            @NotBlank(message = "Elige el motivo") String motivoCodigo,
            @NotBlank(message = "Describe el motivo") @Size(max = 250) String motivoDescripcion,
            @Valid List<ItemIn> items) {}

    public record AnularRequest(@NotBlank(message = "Cuéntanos por qué lo anulas") @Size(max = 100) String motivo) {}

    // ------------------------------------------------------------------ salida

    public record Fila(Long id, TipoComprobante tipo, String serie, int correlativo, String numero, LocalDate fechaEmision,
                       LocalTime horaEmision, String clienteNombre, String clienteNumDoc, BigDecimal total, String moneda,
                       EstadoSunat estadoSunat, EstadoPago estadoPago, String tokenPublico) {

        static Fila de(Comprobante c) {
            return new Fila(c.getId(), c.getTipo(), c.getSerie(), c.getCorrelativo(), c.numeroImpreso(), c.getFechaEmision(),
                    c.getHoraEmision(), c.getClienteNombre(), c.getClienteNumDoc(), c.getTotal(), c.getMoneda(),
                    c.getEstadoSunat(), c.getEstadoPago(), c.getTokenPublico());
        }
    }

    public record EmisorVista(String ruc, String razonSocial, String nombreComercial, String direccion, String distrito,
                              String provincia, String departamento, String telefono, String email, String mensajeTicket,
                              String establecimiento, String ambiente) {}

    public record DetalleVista(int item, String codigo, String descripcion, String unidadMedida, BigDecimal cantidad,
                               BigDecimal precioUnitario, BigDecimal valorUnitario, String tipoAfectacion, BigDecimal valorVenta,
                               BigDecimal igv, BigDecimal icbper, BigDecimal total, Long productoId) {}

    public record PagoVista(Long id, Pago.Metodo metodo, BigDecimal monto, String referencia, Instant pagadoAt) {}

    public record CuotaVista(int numero, BigDecimal monto, LocalDate fechaVencimiento) {}

    public record RefVista(Long id, TipoComprobante tipo, String numero, String token) {}

    public record DetraccionVista(String codigo, String descripcion, BigDecimal porcentaje, BigDecimal monto, String cuentaBanco) {}

    public record Vista(
            Long id, TipoComprobante tipo, String tipoNombre, String serie, int correlativo, String numero,
            LocalDate fechaEmision, LocalTime horaEmision, LocalDate fechaVencimiento, String moneda, FormaPago formaPago,
            String clienteTipoDoc, String clienteNumDoc, String clienteNombre, String clienteDireccion, String clienteEmail,
            String clienteTelefono, BigDecimal igvPorcentaje, BigDecimal totalGravado, BigDecimal totalExonerado,
            BigDecimal totalInafecto, BigDecimal totalIgv, BigDecimal totalIcbper, BigDecimal total, String totalLetras,
            String observaciones, EstadoSunat estadoSunat, String sunatCodigo, String sunatMensaje, String hash,
            EstadoPago estadoPago, BigDecimal montoPagado, BigDecimal saldo, BigDecimal netoACobrar, DetraccionVista detraccion,
            String anulacionMotivo,
            RefVista referencia, String motivoCodigo, String motivoDescripcion, List<RefVista> notas,
            String tokenPublico, String qr, String vendedor, EmisorVista emisor,
            List<DetalleVista> detalles, List<CuotaVista> cuotas, List<PagoVista> pagos, Instant createdAt) {}
}
