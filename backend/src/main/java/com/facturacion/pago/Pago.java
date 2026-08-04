package com.facturacion.pago;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.facturacion.common.MetodoPago;
import com.facturacion.orden.Orden;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagos")
@EntityListeners(AuditingEntityListener.class)
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", unique = true)
    @JsonIgnore
    private Orden orden;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodo;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    @Column(precision = 12, scale = 2)
    private BigDecimal monto;

    private String moneda = "PEN";
    private String referencia;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
