package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.comprobante.Comprobante;

@Entity
@Table(name = "lote_sunat_item")
@Getter
@Setter
public class LoteSunatItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id")
    private LoteSunat lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private int linea;
    /** Solo RC: 1 adicionar, 2 modificar, 3 anular. */
    private String condicion;
    private String motivo;
}
