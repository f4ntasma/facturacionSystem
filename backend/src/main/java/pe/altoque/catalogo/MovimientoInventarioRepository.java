package pe.altoque.catalogo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByEmpresaIdAndProductoIdOrderByIdDesc(Long empresaId, Long productoId, Pageable pageable);

    boolean existsByComprobanteIdAndTipo(Long comprobanteId, MovimientoInventario.Tipo tipo);
}
