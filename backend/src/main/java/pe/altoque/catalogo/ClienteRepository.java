package pe.altoque.catalogo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    Optional<Cliente> findByEmpresaIdAndTipoDocumentoAndNumeroDocumento(Long empresaId, String tipo, String numero);

    @Query("""
            select c from Cliente c where c.empresa.id = :empresaId
              and (:q is null or lower(c.nombre) like lower(concat('%', :q, '%')) or c.numeroDocumento like concat(:q, '%'))
              and (:tipo is null or c.tipoDocumento = :tipo)""")
    Page<Cliente> buscar(Long empresaId, String q, String tipo, Pageable pageable);
}
