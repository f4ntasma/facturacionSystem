package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.Establecimiento;
import pe.altoque.usuario.Usuario;

import java.math.BigDecimal;
import java.time.Instant;

/** Turno de caja: apertura, cobros y cuadre al cierre. */
@Entity
@Table(name = "caja")
@Getter
@Setter
public class Caja {

    public enum Estado { ABIERTA, CERRADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "establecimiento_id")
    private Establecimiento establecimiento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.ABIERTA;

    private BigDecimal montoApertura = BigDecimal.ZERO;
    private BigDecimal montoEsperado;
    private BigDecimal montoDeclarado;
    private String notas;
    private Instant abiertaAt;
    private Instant cerradaAt;

    /** Sobrante (+) o faltante (−) del cuadre; se calcula, no se guarda. */
    public BigDecimal getDiferencia() {
        return montoDeclarado == null || montoEsperado == null ? null : montoDeclarado.subtract(montoEsperado);
    }
}
