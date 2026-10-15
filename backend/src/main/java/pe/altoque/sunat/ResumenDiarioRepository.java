package pe.altoque.sunat;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ResumenDiarioRepository extends JpaRepository<ResumenDiario, Long> {

    List<ResumenDiario> findByEmpresaIdOrderByIdDesc(Long empresaId, Pageable pageable);

    Optional<ResumenDiario> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("select coalesce(max(r.correlativo), 0) from ResumenDiario r where r.empresa.id = :empresaId and r.fechaGeneracion = :fecha")
    int ultimoCorrelativo(Long empresaId, LocalDate fecha);

    @Query("select r.id from ResumenDiario r where r.estado = :estado")
    List<Long> idsPorEstado(LoteSunat.EstadoLote estado);
}
