package pe.altoque.sunat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ComunicacionBajaRepository extends JpaRepository<ComunicacionBaja, Long> {

    List<ComunicacionBaja> findByEmpresaIdOrderByIdDesc(Long empresaId, Pageable pageable);

    Optional<ComunicacionBaja> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("select coalesce(max(b.correlativo), 0) from ComunicacionBaja b where b.empresa.id = :empresaId and b.fechaGeneracion = :fecha")
    int ultimoCorrelativo(Long empresaId, LocalDate fecha);

    @Query("select b.id from ComunicacionBaja b where b.estado = :estado")
    List<Long> idsPorEstado(LoteSunat.EstadoLote estado);
}
