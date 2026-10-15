package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;
import pe.altoque.referencia.Distrito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * El negocio que emite comprobantes (tenant). Lo sensible de SUNAT (ambiente, clave SOL, cuenta de
 * detracciones) vive en la tabla empresa_sunat, 1:1, mapeada aquí como tabla secundaria.
 */
@Entity
@Table(name = "empresa")
@SecondaryTable(name = "empresa_sunat", pkJoinColumns = @PrimaryKeyJoinColumn(name = "empresa_id"))
@SecondaryRow(table = "empresa_sunat", optional = false)
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ubigeo")
    private Distrito ubicacion;

    private String urbanizacion;
    private String telefono;
    private String email;
    private String mensajeTicket;
    private BigDecimal igvPorcentaje = new BigDecimal("18.00");

    @Enumerated(EnumType.STRING)
    @Column(table = "empresa_sunat", name = "ambiente", length = 12)
    private Ambiente ambienteSunat = Ambiente.BETA;

    @Column(table = "empresa_sunat")
    private String solUsuario;

    @Column(table = "empresa_sunat")
    private String solClaveEnc;

    @Column(table = "empresa_sunat")
    private String cuentaDetracciones;

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

    public String getUbigeo() {
        return ubicacion.getUbigeo();
    }

    public String getDistrito() {
        return ubicacion.getNombre();
    }

    public String getProvincia() {
        return ubicacion.getProvincia().getNombre();
    }

    public String getDepartamento() {
        return ubicacion.departamentoNombre();
    }

    public boolean solConfigurado() {
        return solUsuario != null && solClaveEnc != null;
    }
}
