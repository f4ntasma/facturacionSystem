package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.comprobante.Comprobante;

@Entity
@Table(name = "resumen_diario_item")
@Getter
@Setter
public class ResumenDiarioItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resumen_id")
    private ResumenDiario resumen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private int linea;
    /** 1 adicionar, 2 modificar, 3 anular. */
    private String condicion;
}
