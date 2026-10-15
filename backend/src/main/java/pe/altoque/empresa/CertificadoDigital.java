package pe.altoque.empresa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "certificado_digital")
@Getter
@Setter
public class CertificadoDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] archivo;

    private String claveEnc;
    private String sujeto;
    private String emisor;
    private String numeroSerie;
    private Instant validoDesde;
    private Instant validoHasta;
    private boolean autogenerado;
    private boolean activo = true;
    private Instant createdAt;

    @PrePersist
    void alCrear() {
        createdAt = Instant.now();
    }
}
