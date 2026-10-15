package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Resumen diario (RC-AAAAMMDD-n): informa o anula boletas y sus notas. */
@Entity
@Table(name = "resumen_diario")
@Getter
@Setter
public class ResumenDiario implements LoteSunat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    private String identificador;
    private LocalDate fechaReferencia;
    private LocalDate fechaGeneracion;
    private int correlativo;

    @Enumerated(EnumType.STRING)
    private EstadoLote estado = EstadoLote.GENERADO;

    private String ticket;
    private String sunatCodigo;
    private String sunatMensaje;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] xml;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    private byte[] cdr;

    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "resumen", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("linea")
    private List<ResumenDiarioItem> items = new ArrayList<>();

    @PrePersist
    void alCrear() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }
}
