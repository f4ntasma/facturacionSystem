package pe.altoque.empresa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    @EntityGraph(attributePaths = "plan")
    List<Suscripcion> findByEmpresaIdOrderByIdDesc(Long empresaId);

    Optional<Suscripcion> findByIdAndEmpresaId(Long id, Long empresaId);
}
