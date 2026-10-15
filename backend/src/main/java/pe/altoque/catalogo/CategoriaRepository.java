package pe.altoque.catalogo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findByEmpresaIdOrderByNombre(Long empresaId);

    Optional<Categoria> findByEmpresaIdAndNombreIgnoreCase(Long empresaId, String nombre);

    Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId);
}
