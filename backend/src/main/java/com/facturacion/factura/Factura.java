package com.facturacion.factura;

import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "facturas")
@EntityListeners(AuditingEntityListener.class)
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String clienteNombre;
    private String clienteApellido;
    private String clienteDni;
    private String clienteRuc;

    @Enumerated(EnumType.STRING)
    private TipoComprobante tipoComprobante;

    @Enumerated(EnumType.STRING)
    private EstadoFactura estado = EstadoFactura.EMITIDA;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal igv;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "factura", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FacturaItem> items = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteApellido() { return clienteApellido; }
    public void setClienteApellido(String clienteApellido) { this.clienteApellido = clienteApellido; }
    public String getClienteDni() { return clienteDni; }
    public void setClienteDni(String clienteDni) { this.clienteDni = clienteDni; }
    public String getClienteRuc() { return clienteRuc; }
    public void setClienteRuc(String clienteRuc) { this.clienteRuc = clienteRuc; }
    public TipoComprobante getTipoComprobante() { return tipoComprobante; }
    public void setTipoComprobante(TipoComprobante tipoComprobante) { this.tipoComprobante = tipoComprobante; }
    public EstadoFactura getEstado() { return estado; }
    public void setEstado(EstadoFactura estado) { this.estado = estado; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getIgv() { return igv; }
    public void setIgv(BigDecimal igv) { this.igv = igv; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<FacturaItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
}