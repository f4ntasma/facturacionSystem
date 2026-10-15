package pe.altoque.empresa;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import pe.altoque.comprobante.TipoComprobante;

import java.util.List;
import java.util.Optional;

public interface SerieRepository extends JpaRepository<Serie, Long> {

    @Query("select s from Serie s join fetch s.establecimiento where s.empresa.id = :empresaId order by s.tipoComprobante, s.serie")
    List<Serie> listar(Long empresaId);

    Optional<Serie> findByIdAndEmpresaId(Long id, Long empresaId);

    boolean existsByEmpresaIdAndTipoComprobanteAndSerie(Long empresaId, TipoComprobante tipo, String serie);

    /** Bloquea la fila para que dos ventas simultáneas no tomen el mismo correlativo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Serie s where s.id = :id")
    Optional<Serie> bloquear(Long id);

    @Query("""
            select s from Serie s
            where s.empresa.id = :empresaId and s.establecimiento.id = :establecimientoId
              and s.tipoComprobante = :tipo and s.activo = true and s.serie like concat(:prefijo, '%')
            order by s.serie""")
    List<Serie> activas(Long empresaId, Long establecimientoId, TipoComprobante tipo, String prefijo);
}
