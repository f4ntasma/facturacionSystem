package com.facturacion.orden;

import com.facturacion.common.MetodoPago;
import com.facturacion.common.TipoComprobante;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequest {
    @NotNull
    private TipoComprobante tipoComprobante;

    @NotNull
    private MetodoPago metodoPago;

    // Datos del cliente (opcionales; RUC obligatorio si tipoComprobante = FACTURA)
    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private String clienteRuc;

    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }

    public String getClienteApellido() { return clienteApellido; }
    public void setClienteApellido(String clienteApellido) { this.clienteApellido = clienteApellido; }

    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }

    public String getClienteRuc() { return clienteRuc; }
    public void setClienteRuc(String clienteRuc) { this.clienteRuc = clienteRuc; }
}
