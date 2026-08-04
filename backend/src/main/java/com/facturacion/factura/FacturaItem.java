package com.facturacion.factura;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "factura_items")
public class FacturaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Factura factura;

    private String codProducto;
    private String unidad;
    private String descripcion;
    private String productoNombre; // backward compat

    private Integer cantidad;
    private Integer tipAfeIgv; // 10=Gravado, 20=Exonerado, 30=Inafecto, 40=Exportacion

    @Column(precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(precision = 12, scale = 2)
    private BigDecimal mtoValorVenta; // valor sin IGV = cantidad * precioUnitario

    @Column(precision = 12, scale = 2)
    private BigDecimal igv; // IGV de este item

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal; // backward compat = mtoValorVenta

    public UUID getId() { return id; }
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public String getCodProducto() { return codProducto; }
    public void setCodProducto(String codProducto) { this.codProducto = codProducto; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public Integer getTipAfeIgv() { return tipAfeIgv; }
    public void setTipAfeIgv(Integer tipAfeIgv) { this.tipAfeIgv = tipAfeIgv; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getMtoValorVenta() { return mtoValorVenta; }
    public void setMtoValorVenta(BigDecimal mtoValorVenta) { this.mtoValorVenta = mtoValorVenta; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
