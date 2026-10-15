package pe.altoque.sunat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.altoque.comprobante.ComprobanteRepository;

/** Reintenta los comprobantes pendientes y cierra los tickets de anulación, cada 5 minutos. */
@Component
public class SunatScheduler {

    private static final Logger log = LoggerFactory.getLogger(SunatScheduler.class);
    private static final int MAX_INTENTOS = 12;

    private final ComprobanteRepository comprobantes;
    private final ResumenDiarioRepository resumenes;
    private final ComunicacionBajaRepository bajas;
    private final SunatService sunat;

    public SunatScheduler(ComprobanteRepository comprobantes, ResumenDiarioRepository resumenes, ComunicacionBajaRepository bajas,
                          SunatService sunat) {
        this.comprobantes = comprobantes;
        this.resumenes = resumenes;
        this.bajas = bajas;
        this.sunat = sunat;
    }

    @Scheduled(cron = "${altoque.sunat.reintento-cron}", zone = "America/Lima")
    public void trabajar() {
        for (Long id : comprobantes.pendientesDeEnvio(MAX_INTENTOS, PageRequest.of(0, 50))) {
            try {
                sunat.enviar(id);
            } catch (Exception e) {
                log.debug("Reintento de {} falló: {}", id, e.getMessage());
            }
        }
        resumenes.idsPorEstado(LoteSunat.EstadoLote.ENVIADO).forEach(id -> consultar(true, id));
        bajas.idsPorEstado(LoteSunat.EstadoLote.ENVIADO).forEach(id -> consultar(false, id));
    }

    private void consultar(boolean resumen, Long id) {
        try {
            sunat.consultarLote(resumen, id);
        } catch (Exception e) {
            log.debug("Consulta de ticket {} falló: {}", id, e.getMessage());
        }
    }
}
