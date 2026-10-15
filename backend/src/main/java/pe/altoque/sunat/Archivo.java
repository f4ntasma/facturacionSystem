package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** XML firmado o CDR de un comprobante o de un lote. El contenido está en disco; aquí, dónde y su huella. */
@Entity
@Table(name = "archivo")
@Getter
@Setter
public class Archivo {

    public enum Tipo { XML, CDR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empresaId;
    private Long comprobanteId;
    private Long loteId;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private String nombre;
    private String ruta;
    private int bytes;
    private String sha256;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
