package com.facturacion.factura;

import com.facturacion.common.MetodoPago;
import com.facturacion.common.TipoComprobante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class FacturaRequest {

    // Documento
    @NotBlank private String serie;
    // Opcional: el backend lo asigna automaticamente (ultimo + 1 por serie).
    // Se mantiene el campo por compatibilidad pero se ignora su valor.
    private String correlativo;
    @NotNull  private TipoComprobante tipoComprobante;
    private String tipoOperacion = "0101";
    private String moneda = "PEN";
    private String fechaEmision;
    private String fechaVencimiento;

    // Cliente
    @NotBlank private String tipoDocIdentidad;
    @NotBlank private String nroDocIdentidad;
    @NotBlank private String clienteNombre;
    private String clienteDireccion;

    // Pago y adicional
    @NotNull private MetodoPago metodoPago;
    private String observacion;
    private String leyenda;
    private String nroOrdenCompra;
    private String guiaRemision;

    // Items
    @NotEmpty private List<ItemRequest> items;

    // Opcional: venta (orden) de la que proviene este comprobante.
    // Si se envia, la orden queda vinculada a la factura creada.
    private UUID ordenId;

    // --- Getters / Setters ---
    public String getSerie() { return serie; }
    public void setSerie(String v) { this.serie = v; }
    public String getCorrelativo() { return correlativo; }
    public void setCorrelativo(String v) { this.correlativo = v; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante v) { this.tipoComprobante = v; }
    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String v) { this.tipoOperacion = v; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String v) { this.moneda = v; }
    public String getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(String v) { this.fechaEmision = v; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String v) { this.fechaVencimiento = v; }
    public String getTipoDocIdentidad() { return tipoDocIdentidad; }
    public void setTipoDocIdentidad(String v) { this.tipoDocIdentidad = v; }
    public String getNroDocIdentidad() { return nroDocIdentidad; }
    public void setNroDocIdentidad(String v) { this.nroDocIdentidad = v; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String v) { this.clienteNombre = v; }
    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String v) { this.clienteDireccion = v; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago v) { this.metodoPago = v; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String v) { this.observacion = v; }
    public String getLeyenda() { return leyenda; }
    public void setLeyenda(String v) { this.leyenda = v; }
    public String getNroOrdenCompra() { return nroOrdenCompra; }
    public void setNroOrdenCompra(String v) { this.nroOrdenCompra = v; }
    public String getGuiaRemision() { return guiaRemision; }
    public void setGuiaRemision(String v) { this.guiaRemision = v; }
    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> v) { this.items = v; }
    public UUID getOrdenId() { return ordenId; }
    public void setOrdenId(UUID v) { this.ordenId = v; }

    public static class ItemRequest {
        private String codProducto;
        private String unidad = "NIU";
        @NotBlank private String descripcion;
        @NotNull  private Integer cantidad;
        @NotNull  private BigDecimal mtoValorUnitario;
        private Integer tipAfeIgv = 10;

        public String getCodProducto() { return codProducto; }
        public void setCodProducto(String v) { this.codProducto = v; }
        public String getUnidad() { return unidad; }
        public void setUnidad(String v) { this.unidad = v; }
        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String v) { this.descripcion = v; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer v) { this.cantidad = v; }
        public BigDecimal getMtoValorUnitario() { return mtoValorUnitario; }
        public void setMtoValorUnitario(BigDecimal v) { this.mtoValorUnitario = v; }
        public Integer getTipAfeIgv() { return tipAfeIgv; }
        public void setTipAfeIgv(Integer v) { this.tipAfeIgv = v; }
    }
}
