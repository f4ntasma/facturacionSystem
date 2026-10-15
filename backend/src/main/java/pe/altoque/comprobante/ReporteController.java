package pe.altoque.comprobante;

import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.altoque.common.ApiException;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.seguridad.Sesion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Números del negocio: ventas netas por día, por tipo, por medio de pago y lo más vendido. */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final List<EstadoSunat> NO_CUENTAN = List.of(EstadoSunat.RECHAZADO, EstadoSunat.ANULADO);

    public record Dia(LocalDate fecha, long comprobantes, BigDecimal total) {}

    public record Tipo(TipoComprobante tipo, long cantidad, BigDecimal total) {}

    public record Metodo(Pago.Metodo metodo, long cantidad, BigDecimal total) {}

    public record Top(String descripcion, BigDecimal cantidad, BigDecimal total) {}

    public record Resumen(LocalDate desde, LocalDate hasta, BigDecimal ventasNetas, long comprobantes, BigDecimal ticketPromedio,
                          BigDecimal ventasHoy, long comprobantesHoy, BigDecimal porCobrar, long pendientesSunat,
                          List<Dia> dias, List<Tipo> porTipo, List<Metodo> porMetodo, List<Top> topProductos) {}

    private final ComprobanteRepository comprobantes;
    private final PagoRepository pagos;

    public ReporteController(ComprobanteRepository comprobantes, PagoRepository pagos) {
        this.comprobantes = comprobantes;
        this.pagos = pagos;
    }

    @GetMapping("/resumen")
    @Transactional(readOnly = true)
    public Resumen resumen(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        Long empresa = Sesion.actual().empresaId();
        LocalDate hoy = LocalDate.now(LIMA);
        LocalDate fin = hasta != null ? hasta : hoy;
        LocalDate ini = desde != null ? desde : fin.minusDays(6);
        if (ini.isAfter(fin)) throw ApiException.invalido("La fecha inicial es mayor que la final.");
        if (ChronoUnit.DAYS.between(ini, fin) > 366) throw ApiException.invalido("El rango máximo es de un año.");

        Map<LocalDate, BigDecimal> totalDia = new TreeMap<>();
        Map<LocalDate, Long> cantDia = new TreeMap<>();
        for (LocalDate d = ini; !d.isAfter(fin); d = d.plusDays(1)) {
            totalDia.put(d, BigDecimal.ZERO);
            cantDia.put(d, 0L);
        }
        Map<TipoComprobante, long[]> cantTipo = new EnumMap<>(TipoComprobante.class);
        Map<TipoComprobante, BigDecimal> totalTipo = new EnumMap<>(TipoComprobante.class);
        for (var v : comprobantes.ventasPorDia(empresa, ini, fin, NO_CUENTAN)) {
            BigDecimal signo = v.getTipo() == TipoComprobante.NOTA_CREDITO ? v.getTotal().negate() : v.getTotal();
            totalDia.merge(v.getFecha(), signo, BigDecimal::add);
            if (!v.getTipo().esNota()) cantDia.merge(v.getFecha(), v.getCantidad(), Long::sum);
            cantTipo.computeIfAbsent(v.getTipo(), k -> new long[1])[0] += v.getCantidad();
            totalTipo.merge(v.getTipo(), v.getTotal(), BigDecimal::add);
        }
        List<Dia> dias = totalDia.keySet().stream().map(d -> new Dia(d, cantDia.get(d), totalDia.get(d))).toList();
        BigDecimal netas = dias.stream().map(Dia::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        long cantidad = dias.stream().mapToLong(Dia::comprobantes).sum();
        BigDecimal promedio = cantidad == 0 ? BigDecimal.ZERO : netas.divide(BigDecimal.valueOf(cantidad), 2, java.math.RoundingMode.HALF_UP);

        BigDecimal hoyTotal = BigDecimal.ZERO;
        long hoyCant = 0;
        for (var v : comprobantes.ventasPorDia(empresa, hoy, hoy, NO_CUENTAN)) {
            hoyTotal = hoyTotal.add(v.getTipo() == TipoComprobante.NOTA_CREDITO ? v.getTotal().negate() : v.getTotal());
            if (!v.getTipo().esNota()) hoyCant += v.getCantidad();
        }

        var metodos = pagos.porMetodo(empresa, ini.atStartOfDay(LIMA).toInstant(), fin.plusDays(1).atStartOfDay(LIMA).toInstant()).stream()
                .map(m -> new Metodo(m.getMetodo(), m.getCantidad(), m.getTotal()))
                .sorted(Comparator.comparing(Metodo::total).reversed()).toList();
        var top = comprobantes.topProductos(empresa, ini, fin, NO_CUENTAN, PageRequest.of(0, 8)).stream()
                .map(t -> new Top(t.getDescripcion(), t.getCantidad(), t.getTotal())).toList();
        var tipos = totalTipo.keySet().stream().map(t -> new Tipo(t, cantTipo.get(t)[0], totalTipo.get(t))).toList();
        long pendientes = comprobantes.count((root, q, cb) -> cb.and(
                cb.equal(root.get("empresa").get("id"), empresa),
                cb.equal(root.get("estadoSunat"), EstadoSunat.PENDIENTE)));

        return new Resumen(ini, fin, netas, cantidad, promedio, hoyTotal, hoyCant, comprobantes.porCobrar(empresa, NO_CUENTAN),
                pendientes, dias, tipos, metodos, top);
    }
}
