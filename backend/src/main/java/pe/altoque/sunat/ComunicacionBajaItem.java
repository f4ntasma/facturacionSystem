package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.comprobante.Comprobante;

@Entity
@Table(name = "comunicacion_baja_item")
@Getter
@Setter
public class ComunicacionBajaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "baja_id")
    private ComunicacionBaja baja;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    private int linea;
    private String motivo;
}
