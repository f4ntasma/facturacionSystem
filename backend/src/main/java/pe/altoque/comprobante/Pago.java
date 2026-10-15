package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.usuario.Usuario;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pago")
@Getter
@Setter
public class Pago {

    public enum Metodo { EFECTIVO, YAPE, PLIN, TARJETA, TRANSFERENCIA, OTRO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @Enumerated(EnumType.STRING)
    private Metodo metodo;

    private BigDecimal monto;
    private String referencia;
    private Instant pagadoAt;
}
