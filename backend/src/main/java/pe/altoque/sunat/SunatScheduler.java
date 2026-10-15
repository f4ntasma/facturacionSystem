package pe.altoque.sunat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.altoque.comprobante.ComprobanteRepository;

import java.util.List;

/**
 * Cada 5 minutos: reintenta los comprobantes pendientes (con espera creciente según los intentos)
 * y envía o consulta los resúmenes y bajas que siguen abiertos.
 */
@Component
public class SunatScheduler {

    private static final Logger log = LoggerFactory.getLogger(SunatScheduler.class);
    /** 12 intentos a ~5 min cubren una caída de una hora; luego se reintenta a mano desde el detalle. */
    private static final int MAX_INTENTOS = 12;

    private final ComprobanteRepository comprobantes;
    private final LoteSunatRepository lotes;
    private final SunatService sunat;

    public SunatScheduler(ComprobanteRepository comprobantes, LoteSunatRepository lotes, SunatService sunat) {
        this.comprobantes = comprobantes;
        this.lotes = lotes;
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
        for (Long id : lotes.idsPorEstado(List.of(LoteSunat.EstadoLote.GENERADO, LoteSunat.EstadoLote.ENVIADO))) {
            try {
                sunat.procesarLote(id);
            } catch (Exception e) {
                log.debug("Lote {} falló: {}", id, e.getMessage());
            }
        }
    }
}
