package com.facturacion.carrito;

import com.facturacion.producto.ProductResponse;

import java.math.BigDecimal;
import java.util.UUID;

public class CarritoItemResponse {
    private UUID id;
    private ProductResponse producto;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public CarritoItemResponse() {
    }

    public CarritoItemResponse(CarritoItem item) {
        this.id = item.getId();
        this.producto = new ProductResponse(item.getProducto());
        this.cantidad = item.getCantidad();
        this.precioUnitario = item.getPrecioUnitario();
        this.subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
    }

    public UUID getId() {
        return id;
    }

    public ProductResponse getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
