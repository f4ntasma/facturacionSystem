package pe.altoque.comprobante;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.common.Montos;
import pe.altoque.comprobante.Comprobante.EstadoPago;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.comprobante.ComprobanteDtos.*;
import pe.altoque.seguridad.Sesion;
import pe.altoque.sunat.EnvioSunat;
import pe.altoque.sunat.EnvioSunatRepository;
import pe.altoque.sunat.SunatService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    private static final Logger log = LoggerFactory.getLogger(ComprobanteController.class);

    private final ComprobanteService service;
    private final SunatService sunat;
    private final ComprobanteArchivoRepository archivos;
    private final EnvioSunatRepository envios;

    public ComprobanteController(ComprobanteService service, SunatService sunat, ComprobanteArchivoRepository archivos,
                                 EnvioSunatRepository envios) {
        this.service = service;
        this.sunat = sunat;
        this.archivos = archivos;
        this.envios = envios;
    }

    @GetMapping
    public Page<Fila> listar(@RequestParam(required = false) String tipo,
                             @RequestParam(required = false) EstadoSunat estado,
                             @RequestParam(required = false) EstadoPago estadoPago,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                             @RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "0") int pagina,
                             @RequestParam(defaultValue = "20") int tamano) {
        return service.listar(Sesion.actual(), tipo, estado, estadoPago, desde, hasta, q, pagina, tamano);
    }

    @GetMapping("/{id}")
    public Vista ver(@PathVariable Long id) {
        return service.vista(Sesion.actual(), id);
    }

    /** Emite una boleta o factura y la envía a SUNAT al toque. */
    @PostMapping
    public Vista emitir(@Valid @RequestBody EmitirRequest req) {
        Sesion s = Sesion.actual();
        Comprobante c = service.emitir(s, req);
        enviarSinFallar(c.getId());
        return service.vista(s, c.getId());
    }

    @PostMapping("/{id}/notas")
    public Vista emitirNota(@PathVariable Long id, @Valid @RequestBody NotaRequest req) {
        Sesion s = Sesion.actual();
        Comprobante c = service.emitirNota(s, id, req);
        enviarSinFallar(c.getId());
        return service.vista(s, c.getId());
    }

    @PostMapping("/{id}/reenviar")
    public Vista reenviar(@PathVariable Long id) {
        Sesion s = Sesion.actual();
        Comprobante c = service.buscar(s, id);
        if (c.getEstadoSunat() != EstadoSunat.PENDIENTE) throw ApiException.invalido("Este comprobante ya tiene respuesta de SUNAT.");
        sunat.enviar(id);
        return service.vista(s, id);
    }

    @PostMapping("/{id}/pagos")
    public Vista cobrar(@PathVariable Long id, @Valid @RequestBody PagoIn in) {
        Sesion s = Sesion.actual();
        service.registrarPago(s, id, in);
        return service.vista(s, id);
    }

    @PostMapping("/{id}/anular")
    public Vista anular(@PathVariable Long id, @Valid @RequestBody AnularRequest req) {
        Sesion s = Sesion.actual();
        service.anular(s, id, req.motivo());
        return service.vista(s, id);
    }

    @GetMapping("/{id}/envios")
    public List<EnvioSunat> bitacora(@PathVariable Long id) {
        service.buscar(Sesion.actual(), id);
        return envios.findByComprobanteIdOrderByIdDesc(id);
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> xml(@PathVariable Long id) {
        Comprobante c = service.buscar(Sesion.actual(), id);
        return descarga(sunat.xmlDe(id), c.getNombreArchivo() + ".xml", MediaType.APPLICATION_XML);
    }

    @GetMapping("/{id}/cdr")
    public ResponseEntity<byte[]> cdr(@PathVariable Long id) {
        service.buscar(Sesion.actual(), id);
        ComprobanteArchivo a = archivos.findByComprobanteIdAndTipo(id, ComprobanteArchivo.Tipo.CDR)
                .orElseThrow(() -> ApiException.noEncontrado("CDR"));
        return descarga(a.getContenido(), a.getNombre(), MediaType.parseMediaType("application/zip"));
    }

    /** Registro de ventas en CSV (abre directo en Excel). */
    @GetMapping(value = "/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportar(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde.isAfter(hasta)) throw ApiException.invalido("La fecha inicial es mayor que la final.");
        StringBuilder csv = new StringBuilder("﻿Fecha;Tipo;Serie;Número;Doc. cliente;Tipo doc.;Cliente;Moneda;Op. gravada;Op. exonerada;Op. inafecta;IGV;ICBPER;Total;Estado SUNAT;Estado pago;Doc. referencia\n");
        for (Comprobante c : service.paraExportar(Sesion.actual(), desde, hasta)) {
            boolean negativo = c.getTipo() == TipoComprobante.NOTA_CREDITO;
            csv.append(c.getFechaEmision()).append(';')
                    .append(c.getTipo().codigo()).append(';')
                    .append(c.getSerie()).append(';')
                    .append(c.getCorrelativo()).append(';')
                    .append(c.getClienteNumDoc()).append(';')
                    .append(Catalogos.DOCUMENTOS.getOrDefault(c.getClienteTipoDoc(), c.getClienteTipoDoc())).append(';')
                    .append(csvTexto(c.getClienteNombre())).append(';')
                    .append(c.getMoneda()).append(';')
                    .append(monto(c.getTotalGravado(), negativo)).append(';')
                    .append(monto(c.getTotalExonerado(), negativo)).append(';')
                    .append(monto(c.getTotalInafecto(), negativo)).append(';')
                    .append(monto(c.getTotalIgv(), negativo)).append(';')
                    .append(monto(c.getTotalIcbper(), negativo)).append(';')
                    .append(monto(c.getTotal(), negativo)).append(';')
                    .append(c.getEstadoSunat()).append(';')
                    .append(c.getEstadoPago()).append(';')
                    .append(c.getRefSerie() == null ? "" : c.getRefSerie() + "-" + c.getRefCorrelativo())
                    .append('\n');
        }
        return descarga(csv.toString().getBytes(StandardCharsets.UTF_8), "registro-ventas-%s-a-%s.csv".formatted(desde, hasta),
                MediaType.parseMediaType("text/csv; charset=utf-8"));
    }

    private void enviarSinFallar(Long id) {
        try {
            sunat.enviar(id);
        } catch (ApiException e) {
            // p. ej. faltan credenciales SOL: el comprobante queda PENDIENTE y el usuario ve el mensaje en el detalle.
            log.info("Comprobante {} queda pendiente: {}", id, e.getMessage());
            service.marcarMensaje(id, e.getMessage());
        } catch (Exception e) {
            log.warn("Envío a SUNAT falló para {}: {}", id, e.getMessage());
            service.marcarMensaje(id, "No se pudo enviar ahora. Lo reintentamos solito.");
        }
    }

    private static String monto(java.math.BigDecimal v, boolean negativo) {
        String t = Montos.txt(v);
        return negativo && v.signum() != 0 ? "-" + t : t;
    }

    private static String csvTexto(String s) {
        return s == null ? "" : "\"" + s.replace("\"", "\"\"") + "\"";
    }

    static ResponseEntity<byte[]> descarga(byte[] contenido, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre, StandardCharsets.UTF_8).build().toString())
                .contentType(tipo)
                .body(contenido);
    }
}
