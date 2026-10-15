package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "suscripcion")
@Getter
@Setter
public class Suscripcion {

    public enum Estado { PENDIENTE, PAGADA, VENCIDA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private LocalDate periodoInicio;
    private LocalDate periodoFin;
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private Estado estado;

    private String proveedorPago;
    private String referenciaPago;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
