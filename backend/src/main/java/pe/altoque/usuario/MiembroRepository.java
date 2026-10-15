package pe.altoque.usuario;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MiembroRepository extends JpaRepository<Miembro, Long> {

    @EntityGraph(attributePaths = {"usuario", "empresa", "establecimiento"})
    Optional<Miembro> findByUsuarioIdAndEmpresaId(Long usuarioId, Long empresaId);

    /** Negocios a los que la persona puede entrar, el más antiguo primero. */
    @EntityGraph(attributePaths = {"empresa", "establecimiento"})
    @Query("select m from Miembro m where m.usuario.id = :usuarioId and m.activo = true order by m.id")
    List<Miembro> activosDe(Long usuarioId);

    @EntityGraph(attributePaths = {"usuario", "establecimiento"})
    @Query("select m from Miembro m where m.empresa.id = :empresaId order by m.usuario.nombre")
    List<Miembro> equipo(Long empresaId);

    @EntityGraph(attributePaths = {"usuario", "establecimiento"})
    Optional<Miembro> findByIdAndEmpresaId(Long id, Long empresaId);

    long countByEmpresaIdAndActivoTrue(Long empresaId);
}
