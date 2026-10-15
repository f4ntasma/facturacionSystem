package pe.altoque.comprobante;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    @Query("""
            select p.metodo as metodo, count(p) as cantidad, sum(p.monto) as total
            from Pago p join p.comprobante c
            where c.empresa.id = :empresaId and p.pagadoAt >= :desde and p.pagadoAt < :hasta
              and c.estadoSunat <> pe.altoque.comprobante.Comprobante.EstadoSunat.ANULADO
            group by p.metodo""")
    List<PorMetodo> porMetodo(Long empresaId, Instant desde, Instant hasta);

    @Query("""
            select p.metodo as metodo, count(p) as cantidad, sum(p.monto) as total
            from Pago p where p.caja.id = :cajaId group by p.metodo""")
    List<PorMetodo> porMetodoEnCaja(Long cajaId);

    interface PorMetodo {
        Pago.Metodo getMetodo();

        Long getCantidad();

        BigDecimal getTotal();
    }
}
