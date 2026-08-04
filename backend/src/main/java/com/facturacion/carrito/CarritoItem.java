package com.facturacion.carrito;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.facturacion.producto.Producto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "carrito_items")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id")
    @JsonIgnore
    private Carrito carrito;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Integer cantidad;
    private BigDecimal precioUnitario;

    public UUID getId() {
        return id;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
