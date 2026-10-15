package pe.altoque.comprobante;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** XML firmado y CDR de SUNAT. */
@Entity
@Table(name = "comprobante_archivo")
@Getter
@Setter
public class ComprobanteArchivo {

    public enum Tipo { XML, CDR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprobante_id")
    private Comprobante comprobante;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private String nombre;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] contenido;

    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
