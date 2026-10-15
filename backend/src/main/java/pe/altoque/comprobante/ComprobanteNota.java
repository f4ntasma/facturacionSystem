package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Documento que modifica una nota de crédito o débito, y el motivo (catálogos 09 y 10). */
@Entity
@Table(name = "comprobante_nota")
@Getter
@Setter
public class ComprobanteNota {

    @Id
    private Long comprobanteId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private Long empresaId;

    /** null si el documento afectado se emitió fuera de Al Toque. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_ref_id")
    private Comprobante comprobanteRef;

    @Column(name = "ref_tipo")
    private TipoComprobante refTipo;

    private String refSerie;
    private Integer refCorrelativo;

    @Column(name = "nota_tipo")
    private TipoComprobante notaTipo;

    private String motivoCodigo;
    private String sustento;
}
