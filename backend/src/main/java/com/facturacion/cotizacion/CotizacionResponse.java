package com.facturacion.cotizacion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CotizacionResponse {
    private UUID id;
    private String cliente;
    private BigDecimal total;
    private String estado;
    private Instant createdAt;
    private List<ItemResponse> items;

    public CotizacionResponse(Cotizacion c) {
        this.id = c.getId();
        this.cliente = c.getCliente();
        this.total = c.getTotal();
        this.estado = c.getEstado();
        this.createdAt = c.getCreatedAt();
        this.items = c.getItems().stream().map(ItemResponse::new).toList();
    }

    public UUID getId() { return id; }
    public String getCliente() { return cliente; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ItemResponse> getItems() { return items; }

    public static class ItemResponse {
        private String nombre;
        private Integer cantidad;
        private BigDecimal precio;

        public ItemResponse(CotizacionItem i) {
            this.nombre = i.getNombre();
            this.cantidad = i.getCantidad();
            this.precio = i.getPrecio();
        }

        public String getNombre() { return nombre; }
        public Integer getCantidad() { return cantidad; }
        public BigDecimal getPrecio() { return precio; }
    }
}