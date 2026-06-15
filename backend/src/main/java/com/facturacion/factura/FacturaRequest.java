package com.facturacion.factura;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public class FacturaRequest {
    @NotBlank private String clienteNombre;
    @NotBlank private String clienteApellido;
    @NotBlank private String clienteDni;
    private String clienteRuc;
    @NotNull private TipoComprobante tipoComprobante;
    @NotNull private MetodoPago metodoPago;
    @NotEmpty private List<ItemRequest> items;

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String v) { this.clienteNombre = v; }
    public String getClienteApellido() { return clienteApellido; }
    public void setClienteApellido(String v) { this.clienteApellido = v; }
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String v) { this.clienteDni = v; }
    public String getClienteRuc() { return clienteRuc; }
    public void setClienteRuc(String v) { this.clienteRuc = v; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante v) { this.tipoComprobante = v; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago v) { this.metodoPago = v; }
    public List<ItemRequest> getItems() { return items; }
    public void setItems(List<ItemRequest> v) { this.items = v; }

    public static class ItemRequest {
        private String productoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;

        public String getProductoNombre() { return productoNombre; }
        public void setProductoNombre(String v) { this.productoNombre = v; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer v) { this.cantidad = v; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }
    }
}