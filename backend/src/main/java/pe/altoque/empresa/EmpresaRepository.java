package pe.altoque.empresa;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByRuc(String ruc);

    Optional<Empresa> findByRuc(String ruc);

    @EntityGraph(attributePaths = "plan")
    Optional<Empresa> findWithPlanById(Long id);
}
