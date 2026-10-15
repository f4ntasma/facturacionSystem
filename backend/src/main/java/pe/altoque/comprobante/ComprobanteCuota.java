package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "comprobante_cuota")
@Getter
@Setter
public class ComprobanteCuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private int numero;
    private BigDecimal monto;
    private LocalDate fechaVencimiento;
}
