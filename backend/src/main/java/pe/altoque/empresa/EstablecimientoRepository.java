package pe.altoque.empresa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstablecimientoRepository extends JpaRepository<Establecimiento, Long> {

    List<Establecimiento> findByEmpresaIdOrderByCodigoSunat(Long empresaId);

    Optional<Establecimiento> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Establecimiento> findByEmpresaIdAndCodigoSunat(Long empresaId, String codigoSunat);

    long countByEmpresaIdAndActivoTrue(Long empresaId);
}
