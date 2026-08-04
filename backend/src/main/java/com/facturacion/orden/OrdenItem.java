package com.facturacion.orden;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.facturacion.producto.Producto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orden_items")
public class OrdenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id")
    @JsonIgnore
    private Orden orden;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public UUID getId() {
        return id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
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

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
