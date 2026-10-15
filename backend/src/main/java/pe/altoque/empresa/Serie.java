package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.comprobante.TipoComprobante;

import java.time.Instant;

@Entity
@Table(name = "serie")
@Getter
@Setter
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;

    @Column(name = "tipo_comprobante")
    private TipoComprobante tipoComprobante;

    private String serie;
    private int correlativoActual;
    private boolean activo = true;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }

    public int siguiente() {
        return ++correlativoActual;
    }
}
