package pe.altoque.comprobante;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import pe.altoque.comprobante.Comprobante.EstadoSunat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long>, JpaSpecificationExecutor<Comprobante> {

    Optional<Comprobante> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = {"empresa", "establecimiento"})
    Optional<Comprobante> findByTokenPublico(String token);

    Optional<Comprobante> findByEmpresaIdAndTipoAndSerieAndCorrelativo(Long empresaId, TipoComprobante tipo, String serie, int correlativo);

    long countByEmpresaIdAndFechaEmisionBetween(Long empresaId, LocalDate desde, LocalDate hasta);

    List<Comprobante> findByComprobanteRefIdOrderById(Long refId);

    /** Pendientes de envío en ambientes reales, para el reintento automático. */
    @Query("""
            select c.id from Comprobante c
            where c.estadoSunat = pe.altoque.comprobante.Comprobante.EstadoSunat.PENDIENTE
              and c.sunatIntentos < :maxIntentos
              and c.empresa.ambienteSunat <> pe.altoque.empresa.Empresa.Ambiente.SIMULADO
            order by c.id""")
    List<Long> pendientesDeEnvio(int maxIntentos, Pageable pageable);

    @Query("""
            select c.fechaEmision as fecha, c.tipo as tipo, count(c) as cantidad, sum(c.total) as total
            from Comprobante c
            where c.empresa.id = :empresaId and c.fechaEmision between :desde and :hasta
              and c.estadoSunat not in :excluidos
            group by c.fechaEmision, c.tipo
            order by c.fechaEmision""")
    List<VentaDiaria> ventasPorDia(Long empresaId, LocalDate desde, LocalDate hasta, Collection<EstadoSunat> excluidos);

    @Query("""
            select d.descripcion as descripcion, sum(d.cantidad) as cantidad, sum(d.total) as total
            from ComprobanteDetalle d join d.comprobante c
            where c.empresa.id = :empresaId and c.fechaEmision between :desde and :hasta
              and c.tipo in (pe.altoque.comprobante.TipoComprobante.FACTURA, pe.altoque.comprobante.TipoComprobante.BOLETA)
              and c.estadoSunat not in :excluidos
            group by d.descripcion
            order by sum(d.total) desc""")
    List<TopProducto> topProductos(Long empresaId, LocalDate desde, LocalDate hasta, Collection<EstadoSunat> excluidos, Pageable pageable);

    @Query("""
            select coalesce(sum(c.total - c.montoPagado), 0) from Comprobante c
            where c.empresa.id = :empresaId and c.estadoPago <> pe.altoque.comprobante.Comprobante.EstadoPago.PAGADO
              and c.tipo in (pe.altoque.comprobante.TipoComprobante.FACTURA, pe.altoque.comprobante.TipoComprobante.BOLETA)
              and c.estadoSunat not in :excluidos""")
    BigDecimal porCobrar(Long empresaId, Collection<EstadoSunat> excluidos);

    interface VentaDiaria {
        LocalDate getFecha();

        TipoComprobante getTipo();

        Long getCantidad();

        BigDecimal getTotal();
    }

    interface TopProducto {
        String getDescripcion();

        BigDecimal getCantidad();

        BigDecimal getTotal();
    }
}
