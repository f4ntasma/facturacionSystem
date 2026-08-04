package com.facturacion.pago;

import com.facturacion.common.MetodoPago;

import java.math.BigDecimal;
import java.util.UUID;

public class PagoResponse {
    private UUID id;
    private MetodoPago metodo;
    private EstadoPago estado;
    private BigDecimal monto;
    private String moneda;
    private String referencia;

    public PagoResponse(Pago pago) {
        this.id = pago.getId();
        this.metodo = pago.getMetodo();
        this.estado = pago.getEstado();
        this.monto = pago.getMonto();
        this.moneda = pago.getMoneda();
        this.referencia = pago.getReferencia();
    }

    public UUID getId() {
        return id;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getReferencia() {
        return referencia;
    }
}
