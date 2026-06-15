package com.facturacion.factura;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FacturaResponse {
    private UUID id;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private String clienteRuc;
    private String tipoComprobante;
    private String estado;
    private String metodoPago;
    private BigDecimal subtotal;
    private BigDecimal igv;
    private BigDecimal total;
    private Instant createdAt;
    private List<ItemResponse> items;

    public FacturaResponse(Factura f) {
        this.id = f.getId();
        this.clienteNombre = f.getClienteNombre();
        this.clienteApellido = f.getClienteApellido();
        this.clienteDni = f.getClienteDni();
        this.clienteRuc = f.getClienteRuc();
        this.tipoComprobante = f.getTipoComprobante().name();
        this.estado = f.getEstado().name();
        this.metodoPago = f.getMetodoPago().name();
        this.subtotal = f.getSubtotal();
        this.igv = f.getIgv();
        this.total = f.getTotal();
        this.createdAt = f.getCreatedAt();
        this.items = f.getItems().stream().map(ItemResponse::new).toList();
    }

    public UUID getId() { return id; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteApellido() { return clienteApellido; }
    public String getClienteDni() { return clienteDni; }
    public String getClienteRuc() { return clienteRuc; }
    public String getTipoComprobante() { return tipoComprobante; }
    public String getEstado() { return estado; }
    public String getMetodoPago() { return metodoPago; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getIgv() { return igv; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ItemResponse> getItems() { return items; }

    public static class ItemResponse {
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;

        public ItemResponse(FacturaItem i) {
            this.productoNombre = i.getProductoNombre();
            this.cantidad = i.getCantidad();
            this.precioUnitario = i.getPrecioUnitario();
            this.subtotal = i.getSubtotal();
        }

        public String getProductoNombre() { return productoNombre; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public BigDecimal getSubtotal() { return subtotal; }
    }
}