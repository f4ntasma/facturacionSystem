package pe.altoque.comprobante;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CajaRepository extends JpaRepository<Caja, Long> {

    @EntityGraph(attributePaths = "usuario")
    Optional<Caja> findFirstByEmpresaIdAndEstablecimientoIdAndEstadoOrderByIdDesc(Long empresaId, Long establecimientoId, Caja.Estado estado);

    Optional<Caja> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = {"usuario", "establecimiento"})
    List<Caja> findByEmpresaIdOrderByIdDesc(Long empresaId, Pageable pageable);
}
