package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** Bitácora: un registro por cada llamada a SUNAT. */
@Entity
@Table(name = "envio_sunat")
@Getter
@Setter
public class EnvioSunat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long empresaId;
    private Long comprobanteId;
    private Long loteId;
    private String operacion;
    private String ambiente;
    private boolean exito;
    private String codigo;
    private String mensaje;
    private String ticket;
    private Integer duracionMs;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
