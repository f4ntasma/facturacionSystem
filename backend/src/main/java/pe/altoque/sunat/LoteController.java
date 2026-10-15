package pe.altoque.sunat;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.seguridad.Sesion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Resúmenes diarios y comunicaciones de baja (anulaciones) con su ticket SUNAT. */
@RestController
@RequestMapping("/api")
public class LoteController {

    public record LoteVista(Long id, String clase, String identificador, LocalDate fechaReferencia, LocalDate fechaGeneracion,
                            String estado, String ticket, String sunatCodigo, String sunatMensaje, List<String> documentos, Instant createdAt) {}

    private final LoteSunatRepository lotes;
    private final EnvioSunatRepository envios;
    private final SunatService sunat;
    private final Catalogos catalogos;

    public LoteController(LoteSunatRepository lotes, EnvioSunatRepository envios, SunatService sunat, Catalogos catalogos) {
        this.lotes = lotes;
        this.envios = envios;
        this.sunat = sunat;
        this.catalogos = catalogos;
    }

    @GetMapping("/lotes")
    @Transactional(readOnly = true)
    public List<LoteVista> listar() {
        return lotes.findByEmpresaIdOrderByIdDesc(Sesion.actual().empresaId(), PageRequest.of(0, 100)).stream().map(l -> {
            var ultimo = envios.findFirstByLoteIdOrderByIdDesc(l.getId());
            return new LoteVista(l.getId(), l.getTipo() == LoteSunat.Tipo.RC ? "RESUMEN" : "BAJA", l.getIdentificador(),
                    l.getFechaReferencia(), l.getFechaGeneracion(), l.getEstado().name(), l.getTicket(),
                    ultimo.map(EnvioSunat::getCodigo).orElse(null), ultimo.map(EnvioSunat::getMensaje).orElse(null),
                    l.getItems().stream().map(i -> i.getComprobante().numeroImpreso()).toList(), l.getCreatedAt());
        }).toList();
    }

    @PostMapping("/lotes/{id}/consultar")
    public void consultar(@PathVariable Long id) {
        if (lotes.findByIdAndEmpresaId(id, Sesion.actual().empresaId()).isEmpty()) throw ApiException.noEncontrado("Lote");
        sunat.procesarLote(id);
    }

    /** Compatibilidad con la ruta anterior (/lotes/RESUMEN/{id}/consultar). */
    @PostMapping("/lotes/{clase}/{id}/consultar")
    public void consultar(@PathVariable String clase, @PathVariable Long id) {
        consultar(id);
    }

    /** Catálogos para formularios (públicos: no tienen datos del negocio). */
    @GetMapping("/public/catalogos")
    public Map<String, Object> catalogos() {
        return catalogos.paraFormularios();
    }
}
