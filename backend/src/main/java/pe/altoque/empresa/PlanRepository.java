package pe.altoque.empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByCodigo(String codigo);

    List<Plan> findByActivoTrueOrderByOrden();
}
