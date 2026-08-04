package com.facturacion.cotizacion;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cotizacion_items")
public class CotizacionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    private String nombre;
    private Integer cantidad;

    @Column(precision = 12, scale = 2)
    private BigDecimal precio;

    public UUID getId() { return id; }
    public Cotizacion getCotizacion() { return cotizacion; }
    public void setCotizacion(Cotizacion cotizacion) { this.cotizacion = cotizacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
}