package pe.altoque.catalogo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByEmpresaIdAndCodigo(Long empresaId, String codigo);

    Optional<Producto> findByEmpresaIdAndCodigo(Long empresaId, String codigo);

    List<Producto> findByEmpresaIdAndIdIn(Long empresaId, List<Long> ids);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select p from Producto p where p.empresa.id = :empresaId
              and (:soloActivos = false or p.activo = true)
              and (:categoriaId is null or p.categoria.id = :categoriaId)
              and (:q is null or lower(p.nombre) like lower(concat('%', :q, '%')) or lower(p.codigo) like lower(concat(:q, '%')))""")
    Page<Producto> buscar(Long empresaId, String q, Long categoriaId, boolean soloActivos, Pageable pageable);

    @Query("select count(p) from Producto p where p.empresa.id = :empresaId")
    long contar(Long empresaId);
}
