package com.facturacion.cotizacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class CotizacionRequest {
    @NotBlank
    private String cliente;

    @NotEmpty
    private List<ItemRequest> items;

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> items) { this.items = items; }

    public static class ItemRequest {
        private String nombre;
        private Integer cantidad;
        private BigDecimal precio;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecio() { return precio; }
        public void setPrecio(BigDecimal precio) { this.precio = precio; }
    }
}