package pe.altoque.usuario;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = {"empresa", "establecimiento"})
    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = "establecimiento")
    List<Usuario> findByEmpresaIdOrderByNombre(Long empresaId);

    Optional<Usuario> findByIdAndEmpresaId(Long id, Long empresaId);

    long countByEmpresaIdAndActivoTrue(Long empresaId);
}
