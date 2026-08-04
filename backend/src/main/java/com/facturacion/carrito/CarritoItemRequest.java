package com.facturacion.carrito;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CarritoItemRequest {
    @NotNull
    private UUID productoId;

    @Min(1)
    private int cantidad;

    public UUID getProductoId() {
        return productoId;
    }

    public void setProductoId(UUID productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
