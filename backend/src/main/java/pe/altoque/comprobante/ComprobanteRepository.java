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

    @EntityGraph(attributePaths = {"empresa", "serieRef", "serieRef.establecimiento"})
    Optional<Comprobante> findByIdAndEmpresaId(Long id, Long empresaId);

    @EntityGraph(attributePaths = {"empresa", "serieRef", "serieRef.establecimiento"})
    Optional<Comprobante> findByTokenPublico(String token);

    long countByEmpresaIdAndFechaEmisionBetween(Long empresaId, LocalDate desde, LocalDate hasta);

    /** Notas emitidas sobre un comprobante. */
    @Query("select c from Comprobante c join c.nota n where n.comprobanteRef.id = :refId order by c.id")
    List<Comprobante> notasDe(Long refId);

    /**
     * Pendientes de envío que se pueden mandar solos: ambiente real, credenciales listas (o beta)
     * y con menos intentos que el tope. Los que tienen más intentos esperan: el reintento crece.
     */
    @Query("""
            select c.id from Comprobante c
            where c.estadoSunat = pe.altoque.comprobante.Comprobante.EstadoSunat.PENDIENTE
              and (c.empresa.ambienteSunat = pe.altoque.empresa.Empresa.Ambiente.BETA
                   or (c.empresa.ambienteSunat = pe.altoque.empresa.Empresa.Ambiente.PRODUCCION
                       and c.empresa.solUsuario is not null and c.empresa.solClaveEnc is not null))
              and (select count(e) from EnvioSunat e where e.comprobanteId = c.id and e.operacion = 'sendBill') < :maxIntentos
            order by c.id""")
    List<Long> pendientesDeEnvio(long maxIntentos, Pageable pageable);

    @Query("""
            select c.fechaEmision as fecha, s.tipoComprobante as tipo, count(c) as cantidad, sum(c.total) as total
            from Comprobante c join c.serieRef s
            where c.empresa.id = :empresaId and c.fechaEmision between :desde and :hasta
              and c.estadoSunat not in :excluidos
            group by c.fechaEmision, s.tipoComprobante
            order by c.fechaEmision""")
    List<VentaDiaria> ventasPorDia(Long empresaId, LocalDate desde, LocalDate hasta, Collection<EstadoSunat> excluidos);

    @Query("""
            select d.descripcion as descripcion, sum(d.cantidad) as cantidad, sum(d.valorVenta + d.igv + d.icbper) as total
            from ComprobanteDetalle d join d.comprobante c join c.serieRef s
            where c.empresa.id = :empresaId and c.fechaEmision between :desde and :hasta
              and s.tipoComprobante in (pe.altoque.comprobante.TipoComprobante.FACTURA, pe.altoque.comprobante.TipoComprobante.BOLETA)
              and c.estadoSunat not in :excluidos
            group by d.descripcion
            order by sum(d.valorVenta + d.igv + d.icbper) desc""")
    List<TopProducto> topProductos(Long empresaId, LocalDate desde, LocalDate hasta, Collection<EstadoSunat> excluidos, Pageable pageable);

    /** Saldo por cobrar: lo neto a cobrar (sin detracción) menos lo cobrado. */
    @Query("""
            select coalesce(sum(c.total - coalesce((select dt.monto from ComprobanteDetraccion dt where dt.comprobante = c), 0)
                                - coalesce((select sum(p.monto) from Pago p where p.comprobante = c), 0)), 0)
            from Comprobante c join c.serieRef s
            where c.empresa.id = :empresaId and c.estadoPago <> pe.altoque.comprobante.Comprobante.EstadoPago.PAGADO
              and s.tipoComprobante in (pe.altoque.comprobante.TipoComprobante.FACTURA, pe.altoque.comprobante.TipoComprobante.BOLETA)
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
