package pe.altoque.sunat;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.seguridad.Sesion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Resúmenes diarios y comunicaciones de baja (anulaciones) con su ticket SUNAT. */
@RestController
@RequestMapping("/api")
public class LoteController {

    public record LoteVista(Long id, String clase, String identificador, LocalDate fechaReferencia, LocalDate fechaGeneracion,
                            String estado, String ticket, String sunatCodigo, String sunatMensaje, List<String> documentos, Instant createdAt) {}

    private final ResumenDiarioRepository resumenes;
    private final ComunicacionBajaRepository bajas;
    private final SunatService sunat;

    public LoteController(ResumenDiarioRepository resumenes, ComunicacionBajaRepository bajas, SunatService sunat) {
        this.resumenes = resumenes;
        this.bajas = bajas;
        this.sunat = sunat;
    }

    @GetMapping("/lotes")
    @Transactional(readOnly = true)
    public List<LoteVista> listar() {
        Long empresa = Sesion.actual().empresaId();
        List<LoteVista> todos = new ArrayList<>();
        resumenes.findByEmpresaIdOrderByIdDesc(empresa, PageRequest.of(0, 50)).forEach(r -> todos.add(new LoteVista(r.getId(), "RESUMEN",
                r.getIdentificador(), r.getFechaReferencia(), r.getFechaGeneracion(), r.getEstado().name(), r.getTicket(), r.getSunatCodigo(),
                r.getSunatMensaje(), r.getItems().stream().map(i -> i.getComprobante().numeroImpreso()).toList(), r.getCreatedAt())));
        bajas.findByEmpresaIdOrderByIdDesc(empresa, PageRequest.of(0, 50)).forEach(b -> todos.add(new LoteVista(b.getId(), "BAJA",
                b.getIdentificador(), b.getFechaReferencia(), b.getFechaGeneracion(), b.getEstado().name(), b.getTicket(), b.getSunatCodigo(),
                b.getSunatMensaje(), b.getItems().stream().map(i -> i.getComprobante().numeroImpreso()).toList(), b.getCreatedAt())));
        todos.sort(Comparator.comparing(LoteVista::createdAt).reversed());
        return todos;
    }

    @PostMapping("/lotes/{clase}/{id}/consultar")
    public void consultar(@PathVariable String clase, @PathVariable Long id) {
        Long empresa = Sesion.actual().empresaId();
        boolean resumen = "RESUMEN".equalsIgnoreCase(clase);
        boolean existe = resumen ? resumenes.findByIdAndEmpresaId(id, empresa).isPresent() : bajas.findByIdAndEmpresaId(id, empresa).isPresent();
        if (!existe) throw ApiException.noEncontrado(resumen ? "Resumen" : "Comunicación de baja");
        sunat.consultarLote(resumen, id);
    }

    /** Catálogos para formularios (públicos: no tienen datos del negocio). */
    @GetMapping("/public/catalogos")
    public Map<String, Map<String, String>> catalogos() {
        return Map.of(
                "documentos", Catalogos.DOCUMENTOS,
                "motivosNotaCredito", Catalogos.MOTIVOS_NC,
                "motivosNotaDebito", Catalogos.MOTIVOS_ND,
                "unidades", Catalogos.UNIDADES,
                "afectaciones", Catalogos.AFECTACIONES);
    }
}
