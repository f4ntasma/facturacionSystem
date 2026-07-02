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
    private String createdAt;   // ISO-8601 string para que el frontend lo parsee con new Date()
    private PagoResponse pago;
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;

    public OrdenResponse(Orden orden) {
        this.id = orden.getId();
        this.estado = orden.getEstado();
        this.tipoComprobante = orden.getTipoComprobante();
        this.subtotal = orden.getSubtotal();
        this.impuesto = orden.getImpuesto();
        this.total = orden.getTotal();
        this.items = orden.getItems().stream().map(OrdenItemResponse::new).toList();
        this.comprobanteUrl = orden.getComprobanteUrl();
        // Serializar Instant como ISO string (evita ambigüedad segundos vs ms en el frontend)
        this.createdAt = orden.getCreatedAt() != null ? orden.getCreatedAt().toString() : null;
        this.clienteNombre   = orden.getClienteNombre();
        this.clienteApellido = orden.getClienteApellido();
        this.clienteDni      = orden.getClienteDni();
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

    public String getCreatedAt() { return createdAt; }

    public String getClienteNombre()   { return clienteNombre; }
    public String getClienteApellido() { return clienteApellido; }
    public String getClienteDni()      { return clienteDni; }

    public void setPago(PagoResponse pago) { this.pago = pago; }
}
