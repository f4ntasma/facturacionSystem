package pe.altoque.comprobante;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.comprobante.ComprobanteDtos.Vista;
import pe.altoque.sunat.SunatService;

import java.time.Duration;

/** Enlace que recibe el cliente por WhatsApp o al escanear el QR: sin login, solo con el token. */
@RestController
@RequestMapping("/api/public/c/{token}")
public class PublicoController {

    private final ComprobanteRepository comprobantes;
    private final ComprobanteVistas vistas;
    private final SunatService sunat;

    public PublicoController(ComprobanteRepository comprobantes, ComprobanteVistas vistas, SunatService sunat) {
        this.comprobantes = comprobantes;
        this.vistas = vistas;
        this.sunat = sunat;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Vista ver(@PathVariable String token) {
        return vistas.de(buscar(token));
    }

    @GetMapping(value = "/qr.svg", produces = "image/svg+xml")
    @Transactional(readOnly = true)
    public ResponseEntity<String> qr(@PathVariable String token) {
        Comprobante c = buscar(token);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(QrSvg.generar(ComprobanteVistas.textoQr(c)));
    }

    @GetMapping("/xml")
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> xml(@PathVariable String token) {
        Comprobante c = buscar(token);
        return ComprobanteController.descarga(sunat.xmlDe(c.getId()), c.getNombreArchivo() + ".xml", MediaType.APPLICATION_XML);
    }

    private Comprobante buscar(String token) {
        if (token == null || token.length() < 10) throw ApiException.noEncontrado("Comprobante");
        return comprobantes.findByTokenPublico(token).orElseThrow(() -> ApiException.noEncontrado("Comprobante"));
    }
}
