package pe.altoque.sunat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.altoque.empresa.Empresa;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Envío asíncrono por ticket: resumen diario (RC, anula boletas y sus notas) o
 * comunicación de baja (RA, anula facturas y sus notas).
 */
@Entity
@Table(name = "lote_sunat")
@Getter
@Setter
public class LoteSunat {

    public enum Tipo { RC, RA }

    public enum EstadoLote { GENERADO, ENVIADO, ACEPTADO, RECHAZADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    private Tipo tipo;

    private LocalDate fechaGeneracion;
    private int correlativo;
    private LocalDate fechaReferencia;

    @Enumerated(EnumType.STRING)
    private EstadoLote estado = EstadoLote.GENERADO;

    private String ticket;
    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("linea")
    private List<LoteSunatItem> items = new ArrayList<>();

    @PrePersist
    void alCrear() {
        createdAt = updatedAt = Instant.now();
    }

    @PreUpdate
    void alActualizar() {
        updatedAt = Instant.now();
    }

    /** RC-20261015-1 / RA-20261015-1 */
    public String getIdentificador() {
        return "%s-%s-%d".formatted(tipo, fechaGeneracion.format(DateTimeFormatter.BASIC_ISO_DATE), correlativo);
    }

    public String nombreArchivo() {
        return empresa.getRuc() + "-" + getIdentificador();
    }

    public void agregar(LoteSunatItem item) {
        item.setLote(this);
        item.setLinea(items.size() + 1);
        items.add(item);
    }
}
