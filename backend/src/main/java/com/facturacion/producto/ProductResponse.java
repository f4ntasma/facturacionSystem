package com.facturacion.producto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductResponse {
    private UUID id;
    private String nombre;
    private String sku;
    private String descripcion;
    private String categoria;
    private BigDecimal precio;
    private Integer stock;

    public ProductResponse() {
    }

    public ProductResponse(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.sku = producto.getSku();
        this.descripcion = producto.getDescripcion();
        this.categoria = producto.getCategoria();
        this.precio = producto.getPrecio();
        this.stock = producto.getStock();
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getSku() {
        return sku;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Integer getStock() {
        return stock;
    }
}
