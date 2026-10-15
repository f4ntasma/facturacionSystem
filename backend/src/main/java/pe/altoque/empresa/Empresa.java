package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** El negocio que emite comprobantes (tenant). */
@Entity
@Table(name = "empresa")
@Getter
@Setter
public class Empresa {

    public enum Ambiente { SIMULADO, BETA, PRODUCCION }

    public enum Estado { PRUEBA, ACTIVA, SUSPENDIDA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private String direccionFiscal;
    private String ubigeo = "150101";
    private String departamento = "LIMA";
    private String provincia = "LIMA";
    private String distrito = "LIMA";
    private String urbanizacion;
    private String telefono;
    private String email;
    private String mensajeTicket;
    private BigDecimal igvPorcentaje = new BigDecimal("18.00");

    @Enumerated(EnumType.STRING)
    private Ambiente ambienteSunat = Ambiente.SIMULADO;

    private String solUsuario;
    private String solClaveEnc;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.PRUEBA;

    private LocalDate pruebaHasta;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void alCrear() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }

    public String nombreVisible() {
        return nombreComercial != null && !nombreComercial.isBlank() ? nombreComercial : razonSocial;
    }
}
