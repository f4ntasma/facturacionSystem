package pe.altoque.empresa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.config.AltoqueProperties;
import pe.altoque.seguridad.Sesion;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Planes y cobro mensual (Mercado Pago Checkout Pro, opcional). */
@RestController
@RequestMapping("/api")
public class SuscripcionController {

    private static final Logger log = LoggerFactory.getLogger(SuscripcionController.class);

    public record PlanPublico(String codigo, String nombre, String descripcion, BigDecimal precioMensual, BigDecimal precioAnual,
                              Integer limiteComprobantes,
                              int maxUsuarios, int maxEstablecimientos, boolean notasCredito, boolean reportesAvanzados,
                              boolean soportePrioritario, boolean destacado) {
        static PlanPublico de(Plan p) {
            return new PlanPublico(p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getPrecioMensual(), p.precioAnualOMensual(), p.getLimiteComprobantes(),
                    p.getMaxUsuarios(), p.getMaxEstablecimientos(), p.isNotasCredito(), p.isReportesAvanzados(),
                    p.isSoportePrioritario(), p.isDestacado());
        }
    }

    public record SuscripcionVista(Long id, String plan, LocalDate periodoInicio, LocalDate periodoFin, BigDecimal monto, String estado) {}

    public record EstadoCuenta(String estado, LocalDate pruebaHasta, PlanPublico plan, List<SuscripcionVista> historial,
                               boolean pagosEnLinea) {}

    /** periodo: MENSUAL (por defecto) o ANUAL. */
    public record CambioPlan(@NotBlank String plan, String periodo) {}

    public record CambioResultado(String mensaje, String urlPago) {}

    private final PlanRepository planes;
    private final EmpresaRepository empresas;
    private final SuscripcionRepository suscripciones;
    private final AltoqueProperties props;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();

    public SuscripcionController(PlanRepository planes, EmpresaRepository empresas, SuscripcionRepository suscripciones,
                                 AltoqueProperties props, ObjectMapper json) {
        this.planes = planes;
        this.empresas = empresas;
        this.suscripciones = suscripciones;
        this.props = props;
        this.json = json;
    }

    @GetMapping("/planes")
    @Transactional(readOnly = true)
    public List<PlanPublico> planes() {
        return planes.findByActivoTrueOrderByOrden().stream().map(PlanPublico::de).toList();
    }

    @GetMapping("/suscripcion")
    @Transactional(readOnly = true)
    public EstadoCuenta estado() {
        Empresa e = empresas.findWithPlanById(Sesion.actual().empresaId()).orElseThrow();
        var historial = suscripciones.findByEmpresaIdOrderByIdDesc(e.getId()).stream()
                .map(s -> new SuscripcionVista(s.getId(), s.getPlan().getNombre(), s.getPeriodoInicio(), s.getPeriodoFin(), s.getMonto(), s.getEstado().name()))
                .toList();
        return new EstadoCuenta(e.getEstado().name(), e.getPruebaHasta(), PlanPublico.de(e.getPlan()), historial, pagosEnLinea());
    }

    @PostMapping("/suscripcion/cambiar")
    @PreAuthorize("hasRole('PROPIETARIO')")
    @Transactional
    public CambioResultado cambiar(@Valid @RequestBody CambioPlan r) {
        Empresa e = empresas.findWithPlanById(Sesion.actual().empresaId()).orElseThrow();
        Plan plan = planes.findByCodigo(r.plan()).filter(Plan::isActivo).orElseThrow(() -> ApiException.noEncontrado("Plan"));
        boolean enPrueba = e.getEstado() == Empresa.Estado.PRUEBA && e.getPruebaHasta() != null && !LocalDate.now().isAfter(e.getPruebaHasta());
        if (enPrueba) {
            e.setPlan(plan);
            return new CambioResultado("Listo, ahora pruebas el plan " + plan.getNombre() + ".", null);
        }
        if (!pagosEnLinea()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El pago en línea no está activo. Escríbenos y te activamos el plan al toque.");
        }
        boolean anual = "ANUAL".equalsIgnoreCase(r.periodo());
        Suscripcion s = new Suscripcion();
        s.setEmpresa(e);
        s.setPlan(plan);
        LocalDate inicio = LocalDate.now();
        s.setPeriodoInicio(inicio);
        s.setPeriodo(anual ? Suscripcion.Periodo.ANUAL : Suscripcion.Periodo.MENSUAL);
        s.setPeriodoFin(anual ? inicio.plusYears(1) : inicio.plusMonths(1));
        s.setMonto(anual ? plan.precioAnualOMensual() : plan.getPrecioMensual());
        s.setEstado(Suscripcion.Estado.PENDIENTE);
        s.setProveedorPago("MERCADOPAGO");
        suscripciones.save(s);
        return new CambioResultado("Te llevamos a Mercado Pago para completar el pago.", crearPreferencia(s));
    }

    /** Mercado Pago avisa aquí cada pago; se consulta el pago para no confiar en lo que llega. */
    @PostMapping("/suscripcion/webhook")
    @Transactional
    public void webhook(@RequestParam Map<String, String> params, @RequestBody(required = false) JsonNode cuerpo) {
        if (!pagosEnLinea()) return;
        String pagoId = params.getOrDefault("data.id", params.get("id"));
        if (pagoId == null && cuerpo != null) pagoId = cuerpo.path("data").path("id").asText(null);
        if (pagoId == null || !pagoId.matches("\\d+")) return;
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.mercadopago.com/v1/payments/" + pagoId))
                    .header("Authorization", "Bearer " + props.mercadopagoToken()).timeout(Duration.ofSeconds(10)).GET().build();
            JsonNode pago = json.readTree(http.send(req, HttpResponse.BodyHandlers.ofString()).body());
            if (!"approved".equals(pago.path("status").asText())) return;
            Long suscripcionId = Long.parseLong(pago.path("external_reference").asText("0"));
            suscripciones.findById(suscripcionId).ifPresent(s -> {
                if (s.getEstado() == Suscripcion.Estado.PAGADA) return;
                s.setEstado(Suscripcion.Estado.PAGADA);
                s.setReferenciaPago(pago.path("id").asText());
                Empresa e = s.getEmpresa();
                e.setPlan(s.getPlan());
                e.setEstado(Empresa.Estado.ACTIVA);
                e.setPruebaHasta(null);
            });
        } catch (Exception ex) {
            log.warn("Webhook de Mercado Pago no procesado: {}", ex.getMessage());
        }
    }

    private boolean pagosEnLinea() {
        return props.mercadopagoToken() != null && !props.mercadopagoToken().isBlank();
    }

    private String crearPreferencia(Suscripcion s) {
        try {
            var cuerpo = Map.of(
                    "items", List.of(Map.of("title", "Al Toque · Plan " + s.getPlan().getNombre()
                                    + (s.getPeriodo() == Suscripcion.Periodo.ANUAL ? " (12 meses)" : " (1 mes)"),
                            "quantity", 1, "currency_id", "PEN", "unit_price", s.getMonto())),
                    "external_reference", String.valueOf(s.getId()),
                    "notification_url", props.apiPublicUrl() + "/api/suscripcion/webhook",
                    "back_urls", Map.of("success", props.frontendUrl() + "/app/ajustes?tab=plan&pago=ok",
                            "failure", props.frontendUrl() + "/app/ajustes?tab=plan&pago=error",
                            "pending", props.frontendUrl() + "/app/ajustes?tab=plan&pago=pendiente"),
                    "auto_return", "approved");
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.mercadopago.com/checkout/preferences"))
                    .header("Authorization", "Bearer " + props.mercadopagoToken())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(cuerpo))).build();
            JsonNode res = json.readTree(http.send(req, HttpResponse.BodyHandlers.ofString()).body());
            String url = res.path("init_point").asText(null);
            if (url == null) throw new IllegalStateException(res.toString());
            return url;
        } catch (Exception ex) {
            log.warn("No se pudo crear la preferencia de pago: {}", ex.getMessage());
            throw new ApiException(HttpStatus.BAD_GATEWAY, "Mercado Pago no respondió. Inténtalo en un ratito.");
        }
    }
}
