package com.facturacion.carrito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CarritoResponse {
    private UUID id;
    private List<CarritoItemResponse> items;
    private BigDecimal subtotal;

    public CarritoResponse(Carrito carrito) {
        this.id = carrito.getId();
        this.items = carrito.getItems().stream().map(CarritoItemResponse::new).toList();
        this.subtotal = items.stream()
                .map(CarritoItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public List<CarritoItemResponse> getItems() {
        return items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
