package com.facturacion.factura;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FacturaResponse {
    private UUID id;
    private String serie;
    private String correlativo;
    private String tipoComprobante;
    private String tipoDocIdentidad;
    private String clienteNombre;
    private String clienteDni;
    private String clienteRuc;
    private String clienteDireccion;
    private String estado;
    private String metodoPago;
    private String moneda;
    private String fechaEmision;
    private BigDecimal mtoOperGravadas;
    private BigDecimal mtoOperExoneradas;
    private BigDecimal mtoOperInafectas;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private Instant createdAt;
    private List<ItemResponse> items;

    public FacturaResponse(Factura f) {
        this.id = f.getId();
        this.serie = f.getSerie();
        this.correlativo = f.getCorrelativo();
        this.tipoComprobante = f.getTipoComprobante().name();
        this.tipoDocIdentidad = f.getTipoDocIdentidad();
        this.clienteNombre = f.getClienteNombre();
        this.clienteDni = f.getClienteDni();
        this.clienteRuc = f.getClienteRuc();
        this.clienteDireccion = f.getClienteDireccion();
        this.estado = f.getEstado().name();
        this.metodoPago = f.getMetodoPago() != null ? f.getMetodoPago().name() : null;
        this.moneda = f.getMoneda();
        this.fechaEmision = f.getFechaEmision();
        this.mtoOperGravadas = f.getMtoOperGravadas();
        this.mtoOperExoneradas = f.getMtoOperExoneradas();
        this.mtoOperInafectas = f.getMtoOperInafectas();
        this.subtotal = f.getSubtotal();
        this.igv = f.getIgv();
        this.total = f.getTotal();
        this.createdAt = f.getCreatedAt();
        this.items = f.getItems().stream().map(ItemResponse::new).toList();
    }

    public UUID getId() { return id; }
    public String getSerie() { return serie; }
    public String getCorrelativo() { return correlativo; }
    public String getTipoComprobante() { return tipoComprobante; }
    public String getTipoDocIdentidad() { return tipoDocIdentidad; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteDni() { return clienteDni; }
    public String getClienteRuc() { return clienteRuc; }
    public String getClienteDireccion() { return clienteDireccion; }
    public String getEstado() { return estado; }
    public String getMetodoPago() { return metodoPago; }
    public String getMoneda() { return moneda; }
    public String getFechaEmision() { return fechaEmision; }
    public BigDecimal getMtoOperGravadas() { return mtoOperGravadas; }
    public BigDecimal getMtoOperExoneradas() { return mtoOperExoneradas; }
    public BigDecimal getMtoOperInafectas() { return mtoOperInafectas; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getIgv() { return igv; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ItemResponse> getItems() { return items; }

    public static class ItemResponse {
        private String codProducto;
        private String unidad;
        private String descripcion;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private Integer tipAfeIgv;
        private BigDecimal mtoValorVenta;
        private BigDecimal igv;

        public ItemResponse(FacturaItem i) {
            this.codProducto = i.getCodProducto();
            this.unidad = i.getUnidad();
            this.descripcion = i.getDescripcion() != null ? i.getDescripcion() : i.getProductoNombre();
            this.cantidad = i.getCantidad();
            this.precioUnitario = i.getPrecioUnitario();
            this.tipAfeIgv = i.getTipAfeIgv();
            this.mtoValorVenta = i.getMtoValorVenta();
            this.igv = i.getIgv();
        }

        public String getCodProducto() { return codProducto; }
        public String getUnidad() { return unidad; }
        public String getDescripcion() { return descripcion; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public Integer getTipAfeIgv() { return tipAfeIgv; }
        public BigDecimal getMtoValorVenta() { return mtoValorVenta; }
        public BigDecimal getIgv() { return igv; }
    }
}
