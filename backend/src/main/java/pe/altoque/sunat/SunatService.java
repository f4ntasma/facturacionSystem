package pe.altoque.sunat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.catalogo.InventarioService;
import pe.altoque.common.ApiException;
import pe.altoque.common.Cifrado;
import pe.altoque.comprobante.Comprobante;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.comprobante.ComprobanteRepository;
import pe.altoque.empresa.Empresa;
import pe.altoque.sunat.CdrLector.Cdr;
import pe.altoque.sunat.LoteSunat.EstadoLote;
import pe.altoque.sunat.SunatClient.Credenciales;
import pe.altoque.sunat.SunatClient.Respuesta;
import pe.altoque.sunat.xml.FirmaXml;
import pe.altoque.sunat.xml.UblBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Genera, firma, envía y consulta en SUNAT. Cada llamada queda en la bitácora envio_sunat.
 *
 * <p>Cómo se lee la respuesta (códigos de retorno SUNAT):
 * <ul>
 *   <li>CDR con código 0: aceptado. 4000 o más: aceptado con observaciones.</li>
 *   <li>2000–3999: rechazado. El número queda consumido y no se reutiliza; se repone el stock.</li>
 *   <li>0100–1999: excepción (SUNAT no procesó: credenciales, servicio, formato del envío). Queda pendiente y se reintenta.</li>
 *   <li>1033: ya estaba registrado (un reintento tras un corte). En producción se recupera el CDR con getStatusCdr.</li>
 *   <li>Sin respuesta: queda pendiente y se reintenta.</li>
 * </ul>
 */
@Service
public class SunatService {

    private static final Logger log = LoggerFactory.getLogger(SunatService.class);

    private final ComprobanteRepository comprobantes;
    private final LoteSunatRepository lotes;
    private final EnvioSunatRepository envios;
    private final AlmacenArchivos almacen;
    private final CertificadoService certificados;
    private final InventarioService inventario;
    private final SunatClient cliente;
    private final Cifrado cifrado;

    public SunatService(ComprobanteRepository comprobantes, LoteSunatRepository lotes, EnvioSunatRepository envios,
                        AlmacenArchivos almacen, CertificadoService certificados, InventarioService inventario,
                        SunatClient cliente, Cifrado cifrado) {
        this.comprobantes = comprobantes;
        this.lotes = lotes;
        this.envios = envios;
        this.almacen = almacen;
        this.certificados = certificados;
        this.inventario = inventario;
        this.cliente = cliente;
        this.cifrado = cifrado;
    }

    // ------------------------------------------------------------------ comprobantes

    /** Arma y firma el XML del comprobante (dentro de la transacción de emisión). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void generarXml(Comprobante c) {
        CertificadoService.Llave llave = certificados.llavePara(c.getEmpresa());
        FirmaXml.Resultado firmado = FirmaXml.firmar(UblBuilder.comprobante(c), llave.clave(), llave.certificado());
        c.setHash(firmado.hash());
        Empresa e = c.getEmpresa();
        almacen.guardarDeComprobante(e.getId(), e.getRuc(), c.getId(), Archivo.Tipo.XML, c.getNombreArchivo() + ".xml", firmado.xml());
    }

    /** Envía el comprobante con sendBill y actualiza su estado según la respuesta. */
    @Transactional
    public Comprobante enviar(Long comprobanteId) {
        Comprobante c = comprobantes.findById(comprobanteId).orElseThrow(() -> ApiException.noEncontrado("Comprobante"));
        if (c.getEstadoSunat() != EstadoSunat.PENDIENTE) return c;
        Empresa e = c.getEmpresa();

        if (e.getAmbienteSunat() == Empresa.Ambiente.SIMULADO) {
            c.setEstadoSunat(EstadoSunat.ACEPTADO);
            registrar(e, c.getId(), null, "sendBill", true, "0",
                    "Modo simulado: el comprobante se generó y firmó, pero no se envió a SUNAT.", null, 0);
            return c;
        }

        Credenciales cred;
        try {
            cred = credenciales(e);
        } catch (ApiException ex) {
            registrar(e, c.getId(), null, "validacion", false, null, ex.getMessage(), null, 0);
            return c;
        }
        byte[] xml = almacen.deComprobante(c.getId(), Archivo.Tipo.XML).orElseThrow(() -> new IllegalStateException("Comprobante sin XML"));
        byte[] zip = Zip.comprimir(c.getNombreArchivo() + ".xml", xml);

        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.sendBill(e.getAmbienteSunat(), cred, c.getNombreArchivo() + ".zip", zip);
        int ms = (int) (System.currentTimeMillis() - t0);

        if (r.ok() && r.cdrZip() != null) {
            aplicarCdr(c, r.cdrZip(), "sendBill", ms);
        } else if (r.errorRed()) {
            registrar(e, c.getId(), null, "sendBill", false, null, r.faultMensaje() + " · Lo reintentamos solito en unos minutos.", null, ms);
        } else {
            int codigo = numero(r.faultCodigo());
            registrar(e, c.getId(), null, "sendBill", false, r.faultCodigo(), r.faultMensaje(), null, ms);
            if (codigo == 1033) {
                recuperarRegistrado(c, cred);
            } else if (codigo >= 2000 && codigo < 4000) {
                rechazar(c);
            }
            // 0100–1999: excepción; queda PENDIENTE y el reintento automático lo vuelve a mandar.
        }
        return c;
    }

    /** SUNAT dice que ya lo tiene: en producción se baja su CDR; en beta no existe la consulta. */
    private void recuperarRegistrado(Comprobante c, Credenciales cred) {
        Empresa e = c.getEmpresa();
        if (e.getAmbienteSunat() != Empresa.Ambiente.PRODUCCION) {
            c.setEstadoSunat(EstadoSunat.ACEPTADO);
            return;
        }
        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.getStatusCdr(cred, e.getRuc(), c.getTipo().codigo(), c.getSerie(), c.getCorrelativo());
        int ms = (int) (System.currentTimeMillis() - t0);
        if (r.ok() && r.cdrZip() != null) {
            aplicarCdr(c, r.cdrZip(), "getStatusCdr", ms);
        } else {
            registrar(e, c.getId(), null, "getStatusCdr", false, r.statusCode() != null ? r.statusCode() : r.faultCodigo(),
                    r.faultMensaje() != null ? r.faultMensaje() : "SUNAT no devolvió la constancia todavía.", null, ms);
        }
    }

    private void aplicarCdr(Comprobante c, byte[] cdrZip, String operacion, int ms) {
        Empresa e = c.getEmpresa();
        Cdr cdr = CdrLector.leer(Zip.primerXml(cdrZip));
        almacen.guardarDeComprobante(e.getId(), e.getRuc(), c.getId(), Archivo.Tipo.CDR, "R-" + c.getNombreArchivo() + ".zip", cdrZip);
        String obs = cdr.notas().isEmpty() ? "" : " Observaciones: " + String.join(" | ", cdr.notas());
        if (cdr.aceptado()) {
            c.setEstadoSunat(cdr.notas().isEmpty() && cdr.numero() == 0 ? EstadoSunat.ACEPTADO : EstadoSunat.OBSERVADO);
        } else {
            rechazar(c);
        }
        registrar(e, c.getId(), null, operacion, cdr.aceptado(), cdr.codigo(), cdr.descripcion() + obs, null, ms);
    }

    /** Rechazado: nunca tuvo validez. Lo vendido regresa al stock. */
    private void rechazar(Comprobante c) {
        c.setEstadoSunat(EstadoSunat.RECHAZADO);
        inventario.revertirVenta(c);
    }

    // ------------------------------------------------------------------ anulación

    /** Anula ante SUNAT: resumen diario (boletas y sus notas) o comunicación de baja (facturas y sus notas). */
    @Transactional(propagation = Propagation.MANDATORY)
    public LoteSunat anular(Comprobante c, String motivo) {
        Empresa e = c.getEmpresa();
        LocalDate hoy = LocalDate.now();
        LoteSunat lote = new LoteSunat();
        lote.setEmpresa(e);
        lote.setTipo(c.seAnulaPorResumen() ? LoteSunat.Tipo.RC : LoteSunat.Tipo.RA);
        lote.setFechaGeneracion(hoy);
        lote.setFechaReferencia(c.getFechaEmision());
        lote.setCorrelativo(lotes.ultimoCorrelativo(e.getId(), lote.getTipo(), hoy) + 1);
        LoteSunatItem item = new LoteSunatItem();
        item.setComprobante(c);
        item.setCondicion(lote.getTipo() == LoteSunat.Tipo.RC ? "3" : null);
        item.setMotivo(motivo.length() > 100 ? motivo.substring(0, 100) : motivo);
        lote.agregar(item);
        lotes.save(lote);

        String xml = lote.getTipo() == LoteSunat.Tipo.RC ? UblBuilder.resumenDiario(lote) : UblBuilder.comunicacionBaja(lote);
        CertificadoService.Llave llave = certificados.llavePara(e);
        byte[] firmado = FirmaXml.firmar(xml, llave.clave(), llave.certificado()).xml();
        almacen.guardarDeLote(e.getId(), e.getRuc(), lote.getId(), Archivo.Tipo.XML, lote.nombreArchivo() + ".xml", firmado);
        c.setEstadoSunat(EstadoSunat.POR_ANULAR);

        if (e.getAmbienteSunat() == Empresa.Ambiente.SIMULADO) {
            lote.setEstado(EstadoLote.ACEPTADO);
            cerrarAnulacion(lote, true);
            registrar(e, null, lote.getId(), "sendSummary", true, "0", "Modo simulado: anulación registrada sin enviar a SUNAT.", null, 0);
            return lote;
        }
        enviarLote(lote, firmado);
        return lote;
    }

    private void enviarLote(LoteSunat lote, byte[] xmlFirmado) {
        Empresa e = lote.getEmpresa();
        String nombre = lote.nombreArchivo();
        Credenciales cred;
        try {
            cred = credenciales(e);
        } catch (ApiException ex) {
            registrar(e, null, lote.getId(), "validacion", false, null, ex.getMessage(), null, 0);
            return;
        }
        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.sendSummary(e.getAmbienteSunat(), cred, nombre + ".zip", Zip.comprimir(nombre + ".xml", xmlFirmado));
        int ms = (int) (System.currentTimeMillis() - t0);
        if (r.ok() && r.ticket() != null) {
            lote.setTicket(r.ticket());
            lote.setEstado(EstadoLote.ENVIADO);
            registrar(e, null, lote.getId(), "sendSummary", true, null, "Enviado. SUNAT procesa el ticket " + r.ticket() + ".", r.ticket(), ms);
        } else {
            registrar(e, null, lote.getId(), "sendSummary", false, r.faultCodigo(), r.faultMensaje(), null, ms);
            if (!r.errorRed() && numero(r.faultCodigo()) >= 2000) {
                lote.setEstado(EstadoLote.RECHAZADO);
                cerrarAnulacion(lote, false);
            }
        }
    }

    /** Envía un lote que quedó generado, o consulta el ticket de uno enviado y cierra la anulación. */
    @Transactional
    public void procesarLote(Long id) {
        LoteSunat lote = lotes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Lote"));
        Empresa e = lote.getEmpresa();
        if (lote.getEstado() == EstadoLote.GENERADO) {
            byte[] xml = almacen.deLote(lote.getId(), Archivo.Tipo.XML).orElseThrow(() -> new IllegalStateException("Lote sin XML"));
            enviarLote(lote, xml);
            return;
        }
        if (lote.getEstado() != EstadoLote.ENVIADO || lote.getTicket() == null) return;
        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.getStatus(e.getAmbienteSunat(), credenciales(e), lote.getTicket());
        int ms = (int) (System.currentTimeMillis() - t0);
        if (!r.ok()) {
            registrar(e, null, lote.getId(), "getStatus", false, r.faultCodigo(), r.faultMensaje(), lote.getTicket(), ms);
            return;
        }
        if ("98".equals(r.statusCode())) {
            registrar(e, null, lote.getId(), "getStatus", true, "98", "SUNAT aún procesa el ticket.", lote.getTicket(), ms);
            return;
        }
        Cdr cdr = r.cdrZip() != null ? CdrLector.leer(Zip.primerXml(r.cdrZip())) : new Cdr(r.statusCode(), "Sin CDR", List.of());
        if (r.cdrZip() != null) {
            almacen.guardarDeLote(e.getId(), e.getRuc(), lote.getId(), Archivo.Tipo.CDR, "R-" + lote.nombreArchivo() + ".zip", r.cdrZip());
        }
        boolean ok = "0".equals(r.statusCode()) && cdr.aceptado();
        lote.setEstado(ok ? EstadoLote.ACEPTADO : EstadoLote.RECHAZADO);
        cerrarAnulacion(lote, ok);
        registrar(e, null, lote.getId(), "getStatus", ok, cdr.codigo(), cdr.descripcion(), lote.getTicket(), ms);
    }

    private void cerrarAnulacion(LoteSunat lote, boolean aceptada) {
        for (LoteSunatItem item : lote.getItems()) {
            Comprobante c = item.getComprobante();
            if (aceptada) {
                c.setEstadoSunat(EstadoSunat.ANULADO);
                inventario.revertirVenta(c);
            } else {
                // SUNAT no aceptó la anulación: el comprobante sigue vigente.
                c.setEstadoSunat(EstadoSunat.ACEPTADO);
            }
        }
    }

    // ------------------------------------------------------------------ apoyo

    private Credenciales credenciales(Empresa e) {
        if (e.getAmbienteSunat() == Empresa.Ambiente.BETA) {
            // En beta SUNAT acepta cualquier RUC con el usuario MODDATOS.
            return new Credenciales(e.getRuc() + "MODDATOS", "MODDATOS");
        }
        if (!e.solConfigurado()) {
            throw ApiException.invalido("Falta tu usuario y clave SOL secundaria. Complétalos en Ajustes › SUNAT.");
        }
        return new Credenciales(e.getRuc() + e.getSolUsuario().toUpperCase(), cifrado.descifrar(e.getSolClaveEnc()));
    }

    private void registrar(Empresa e, Long comprobanteId, Long loteId, String operacion, boolean exito,
                           String codigo, String mensaje, String ticket, int ms) {
        EnvioSunat env = new EnvioSunat();
        env.setEmpresaId(e.getId());
        env.setComprobanteId(comprobanteId);
        env.setLoteId(loteId);
        env.setOperacion(operacion);
        env.setAmbiente(e.getAmbienteSunat().name());
        env.setExito(exito);
        env.setCodigo(codigo);
        env.setMensaje(recortar(mensaje));
        env.setTicket(ticket);
        env.setDuracionMs(ms);
        envios.save(env);
        if (!exito) log.info("SUNAT {} empresa={} comprobante={} lote={} código={} {}", operacion, e.getRuc(), comprobanteId, loteId, codigo, mensaje);
    }

    /** Deja constancia en la bitácora de algo que impidió enviar (fuera de una llamada a SUNAT). */
    @Transactional
    public void anotar(Long comprobanteId, String mensaje) {
        comprobantes.findById(comprobanteId).ifPresent(c -> {
            if (c.getEstadoSunat() == EstadoSunat.PENDIENTE) registrar(c.getEmpresa(), c.getId(), null, "validacion", false, null, mensaje, null, 0);
        });
    }

    private static int numero(String codigo) {
        try {
            return codigo == null ? -1 : Integer.parseInt(codigo);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String recortar(String s) {
        if (s == null) return null;
        return s.length() > 990 ? s.substring(0, 990) + "…" : s;
    }

    /** XML firmado, para descargar. */
    public byte[] xmlDe(Long comprobanteId) {
        return almacen.deComprobante(comprobanteId, Archivo.Tipo.XML).orElseThrow(() -> ApiException.noEncontrado("XML"));
    }

    public String xmlTexto(Long comprobanteId) {
        return new String(xmlDe(comprobanteId), StandardCharsets.UTF_8);
    }
}
