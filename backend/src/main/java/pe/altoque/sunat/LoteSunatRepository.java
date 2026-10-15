package pe.altoque.sunat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteSunatRepository extends JpaRepository<LoteSunat, Long> {

    @EntityGraph(attributePaths = {"items", "items.comprobante"})
    List<LoteSunat> findByEmpresaIdOrderByIdDesc(Long empresaId, Pageable pageable);

    Optional<LoteSunat> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("""
            select coalesce(max(l.correlativo), 0) from LoteSunat l
            where l.empresa.id = :empresaId and l.tipo = :tipo and l.fechaGeneracion = :fecha""")
    int ultimoCorrelativo(Long empresaId, LoteSunat.Tipo tipo, LocalDate fecha);

    @Query("select l.id from LoteSunat l where l.estado in :estados order by l.id")
    List<Long> idsPorEstado(List<LoteSunat.EstadoLote> estados);

    /** Pedidos de anulación de un comprobante, el último primero (para mostrar el motivo). */
    @Query("select i from LoteSunatItem i join fetch i.lote where i.comprobante.id = :comprobanteId order by i.id desc")
    List<LoteSunatItem> itemsDe(Long comprobanteId);
}
