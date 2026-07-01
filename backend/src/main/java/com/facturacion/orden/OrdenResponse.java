package com.facturacion.orden;

import com.facturacion.pago.PagoResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrdenResponse {
    private UUID id;
    private OrderStatus estado;
    private TipoComprobante tipoComprobante;
    private BigDecimal subtotal;
    private BigDecimal impuesto;
    private BigDecimal total;
    private List<OrdenItemResponse> items;
    private String comprobanteUrl;
    private Instant createdAt;   // FIX: faltaba — frontend lo necesita para mostrar la fecha
    private PagoResponse pago;

    public OrdenResponse(Orden orden) {
        this.id = orden.getId();
        this.estado = orden.getEstado();
        this.tipoComprobante = orden.getTipoComprobante();
        this.subtotal = orden.getSubtotal();
        this.impuesto = orden.getImpuesto();
        this.total = orden.getTotal();
        this.items = orden.getItems().stream().map(OrdenItemResponse::new).toList();
        this.comprobanteUrl = orden.getComprobanteUrl();
        this.createdAt = orden.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public OrderStatus getEstado() {
        return estado;
    }

    public TipoComprobante getTipoComprobante() {
        return tipoComprobante;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<OrdenItemResponse> getItems() {
        return items;
    }

    public String getComprobanteUrl() {
        return comprobanteUrl;
    }

    public PagoResponse getPago() {
        return pago;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setPago(PagoResponse pago) {
        this.pago = pago;
    }
}
