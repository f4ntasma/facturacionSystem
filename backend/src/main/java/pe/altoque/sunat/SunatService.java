package pe.altoque.sunat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.common.Cifrado;
import pe.altoque.comprobante.Comprobante;
import pe.altoque.comprobante.Comprobante.EstadoSunat;
import pe.altoque.comprobante.ComprobanteArchivo;
import pe.altoque.comprobante.ComprobanteArchivoRepository;
import pe.altoque.comprobante.ComprobanteRepository;
import pe.altoque.empresa.Empresa;
import pe.altoque.sunat.CdrLector.Cdr;
import pe.altoque.sunat.LoteSunat.EstadoLote;
import pe.altoque.sunat.SunatClient.Credenciales;
import pe.altoque.sunat.SunatClient.Respuesta;
import pe.altoque.sunat.xml.FirmaXml;
import pe.altoque.sunat.xml.UblBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Genera, firma y envía a SUNAT. Cada llamada queda en la bitácora envio_sunat. */
@Service
public class SunatService {

    private static final Logger log = LoggerFactory.getLogger(SunatService.class);

    private final ComprobanteRepository comprobantes;
    private final ComprobanteArchivoRepository archivos;
    private final ResumenDiarioRepository resumenes;
    private final ComunicacionBajaRepository bajas;
    private final EnvioSunatRepository envios;
    private final CertificadoService certificados;
    private final SunatClient cliente;
    private final Cifrado cifrado;

    public SunatService(ComprobanteRepository comprobantes, ComprobanteArchivoRepository archivos,
                        ResumenDiarioRepository resumenes, ComunicacionBajaRepository bajas, EnvioSunatRepository envios,
                        CertificadoService certificados, SunatClient cliente, Cifrado cifrado) {
        this.comprobantes = comprobantes;
        this.archivos = archivos;
        this.resumenes = resumenes;
        this.bajas = bajas;
        this.envios = envios;
        this.certificados = certificados;
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
        ComprobanteArchivo xml = new ComprobanteArchivo();
        xml.setComprobante(c);
        xml.setTipo(ComprobanteArchivo.Tipo.XML);
        xml.setNombre(c.getNombreArchivo() + ".xml");
        xml.setContenido(firmado.xml());
        archivos.save(xml);
    }

    /** Envía el comprobante con sendBill y actualiza su estado según el CDR. */
    @Transactional
    public Comprobante enviar(Long comprobanteId) {
        Comprobante c = comprobantes.findById(comprobanteId).orElseThrow(() -> ApiException.noEncontrado("Comprobante"));
        if (c.getEstadoSunat() != EstadoSunat.PENDIENTE) return c;
        Empresa e = c.getEmpresa();
        c.setSunatIntentos(c.getSunatIntentos() + 1);
        c.setEnviadoAt(Instant.now());

        if (e.getAmbienteSunat() == Empresa.Ambiente.SIMULADO) {
            c.setEstadoSunat(EstadoSunat.ACEPTADO);
            c.setSunatCodigo("0");
            c.setSunatMensaje("Modo simulado: el comprobante se generó y firmó, pero no se envió a SUNAT.");
            registrar(e, c.getId(), null, null, "sendBill", true, "0", c.getSunatMensaje(), null, 0);
            return c;
        }

        byte[] xml = archivos.findByComprobanteIdAndTipo(c.getId(), ComprobanteArchivo.Tipo.XML)
                .map(ComprobanteArchivo::getContenido)
                .orElseThrow(() -> new IllegalStateException("Comprobante sin XML"));
        byte[] zip = Zip.comprimir(c.getNombreArchivo() + ".xml", xml);

        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.sendBill(e.getAmbienteSunat(), credenciales(e), c.getNombreArchivo() + ".zip", zip);
        int ms = (int) (System.currentTimeMillis() - t0);

        if (r.ok() && r.cdrZip() != null) {
            byte[] cdrXml = Zip.primerXml(r.cdrZip());
            Cdr cdr = CdrLector.leer(cdrXml);
            guardarCdr(c, r.cdrZip());
            c.setSunatCodigo(cdr.codigo());
            String obs = cdr.notas().isEmpty() ? "" : " Observaciones: " + String.join(" | ", cdr.notas());
            c.setSunatMensaje(recortar(cdr.descripcion() + obs));
            if (cdr.aceptado()) {
                c.setEstadoSunat(cdr.notas().isEmpty() && cdr.numero() == 0 ? EstadoSunat.ACEPTADO : EstadoSunat.OBSERVADO);
            } else {
                c.setEstadoSunat(EstadoSunat.RECHAZADO);
            }
            registrar(e, c.getId(), null, null, "sendBill", cdr.aceptado(), cdr.codigo(), c.getSunatMensaje(), null, ms);
        } else if (r.errorRed()) {
            c.setSunatMensaje(recortar(r.faultMensaje() + " · Lo reintentamos solito en unos minutos."));
            registrar(e, c.getId(), null, null, "sendBill", false, null, r.faultMensaje(), null, ms);
        } else {
            int codigo = numero(r.faultCodigo());
            c.setSunatCodigo(r.faultCodigo());
            c.setSunatMensaje(recortar(r.faultMensaje()));
            if (codigo >= 2000 && codigo < 4000) {
                c.setEstadoSunat(EstadoSunat.RECHAZADO);
            } else if (codigo == 1033) {
                // Ya registrado en SUNAT (p. ej. un reintento tras un corte): se considera aceptado.
                c.setEstadoSunat(EstadoSunat.ACEPTADO);
            }
            // 0100–1999: excepción (credenciales, servicio caído…): queda PENDIENTE para reintento.
            registrar(e, c.getId(), null, null, "sendBill", false, r.faultCodigo(), r.faultMensaje(), null, ms);
        }
        return c;
    }

    // ------------------------------------------------------------------ anulación

    /** Anula ante SUNAT: resumen diario (boletas) o comunicación de baja (facturas). */
    @Transactional
    public LoteSunat anular(Comprobante c, String motivo) {
        Empresa e = c.getEmpresa();
        LocalDate hoy = LocalDate.now();
        LoteSunat lote;
        String xml;
        if (c.seAnulaPorResumen()) {
            ResumenDiario r = new ResumenDiario();
            r.setEmpresa(e);
            r.setFechaReferencia(c.getFechaEmision());
            r.setFechaGeneracion(hoy);
            r.setCorrelativo(resumenes.ultimoCorrelativo(e.getId(), hoy) + 1);
            r.setIdentificador("RC-%s-%d".formatted(hoy.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), r.getCorrelativo()));
            ResumenDiarioItem item = new ResumenDiarioItem();
            item.setResumen(r);
            item.setComprobante(c);
            item.setLinea(1);
            item.setCondicion("3");
            r.getItems().add(item);
            xml = UblBuilder.resumenDiario(r);
            lote = r;
        } else {
            ComunicacionBaja b = new ComunicacionBaja();
            b.setEmpresa(e);
            b.setFechaReferencia(c.getFechaEmision());
            b.setFechaGeneracion(hoy);
            b.setCorrelativo(bajas.ultimoCorrelativo(e.getId(), hoy) + 1);
            b.setIdentificador("RA-%s-%d".formatted(hoy.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE), b.getCorrelativo()));
            ComunicacionBajaItem item = new ComunicacionBajaItem();
            item.setBaja(b);
            item.setComprobante(c);
            item.setLinea(1);
            item.setMotivo(motivo.length() > 100 ? motivo.substring(0, 100) : motivo);
            b.getItems().add(item);
            xml = UblBuilder.comunicacionBaja(b);
            lote = b;
        }
        CertificadoService.Llave llave = certificados.llavePara(e);
        byte[] firmado = FirmaXml.firmar(xml, llave.clave(), llave.certificado()).xml();
        if (lote instanceof ResumenDiario r) {
            r.setXml(firmado);
            resumenes.save(r);
        } else {
            ComunicacionBaja b = (ComunicacionBaja) lote;
            b.setXml(firmado);
            bajas.save(b);
        }
        c.setEstadoSunat(EstadoSunat.POR_ANULAR);
        c.setAnulacionMotivo(motivo);

        if (e.getAmbienteSunat() == Empresa.Ambiente.SIMULADO) {
            lote.setEstado(EstadoLote.ACEPTADO);
            lote.setSunatCodigo("0");
            lote.setSunatMensaje("Modo simulado: anulación registrada sin enviar a SUNAT.");
            marcarAnulados(lote, true, null);
            registrar(e, null, idResumen(lote), idBaja(lote), "sendSummary", true, "0", "Simulado", null, 0);
            return lote;
        }
        enviarLote(lote);
        return lote;
    }

    private void enviarLote(LoteSunat lote) {
        Empresa e = lote.getEmpresa();
        String nombre = e.getRuc() + "-" + lote.getIdentificador();
        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.sendSummary(e.getAmbienteSunat(), credenciales(e), nombre + ".zip", Zip.comprimir(nombre + ".xml", lote.getXml()));
        int ms = (int) (System.currentTimeMillis() - t0);
        if (r.ok() && r.ticket() != null) {
            lote.setTicket(r.ticket());
            lote.setEstado(EstadoLote.ENVIADO);
            lote.setSunatMensaje("Enviado. SUNAT está procesando el ticket " + r.ticket() + ".");
            registrar(e, null, idResumen(lote), idBaja(lote), "sendSummary", true, null, null, r.ticket(), ms);
        } else {
            lote.setSunatCodigo(r.faultCodigo());
            lote.setSunatMensaje(recortar(r.faultMensaje()));
            int codigo = numero(r.faultCodigo());
            if (!r.errorRed() && codigo >= 2000) {
                lote.setEstado(EstadoLote.RECHAZADO);
                marcarAnulados(lote, false, r.faultMensaje());
            }
            registrar(e, null, idResumen(lote), idBaja(lote), "sendSummary", false, r.faultCodigo(), r.faultMensaje(), null, ms);
        }
    }

    @Transactional
    public void consultarLote(boolean resumen, Long id) {
        LoteSunat lote = resumen ? resumenes.findById(id).orElseThrow() : bajas.findById(id).orElseThrow();
        consultarTicket(lote);
    }

    /** Consulta el ticket de un resumen o baja enviado y cierra la anulación. */
    private void consultarTicket(LoteSunat lote) {
        Empresa e = lote.getEmpresa();
        if (lote.getEstado() == EstadoLote.GENERADO) {
            enviarLote(lote);
            return;
        }
        if (lote.getEstado() != EstadoLote.ENVIADO || lote.getTicket() == null) return;
        long t0 = System.currentTimeMillis();
        Respuesta r = cliente.getStatus(e.getAmbienteSunat(), credenciales(e), lote.getTicket());
        int ms = (int) (System.currentTimeMillis() - t0);
        if (!r.ok()) {
            registrar(e, null, idResumen(lote), idBaja(lote), "getStatus", false, r.faultCodigo(), r.faultMensaje(), lote.getTicket(), ms);
            return;
        }
        if ("98".equals(r.statusCode())) {
            registrar(e, null, idResumen(lote), idBaja(lote), "getStatus", true, "98", "En proceso", lote.getTicket(), ms);
            return;
        }
        Cdr cdr = r.cdrZip() != null ? CdrLector.leer(Zip.primerXml(r.cdrZip())) : new Cdr(r.statusCode(), "Sin CDR", List.of());
        lote.setCdr(r.cdrZip());
        lote.setSunatCodigo(cdr.codigo());
        lote.setSunatMensaje(recortar(cdr.descripcion()));
        boolean ok = "0".equals(r.statusCode()) && cdr.aceptado();
        lote.setEstado(ok ? EstadoLote.ACEPTADO : EstadoLote.RECHAZADO);
        marcarAnulados(lote, ok, cdr.descripcion());
        registrar(e, null, idResumen(lote), idBaja(lote), "getStatus", ok, cdr.codigo(), cdr.descripcion(), lote.getTicket(), ms);
    }

    private void marcarAnulados(LoteSunat lote, boolean aceptado, String mensaje) {
        List<Comprobante> afectados = lote instanceof ResumenDiario r
                ? r.getItems().stream().map(ResumenDiarioItem::getComprobante).toList()
                : ((ComunicacionBaja) lote).getItems().stream().map(ComunicacionBajaItem::getComprobante).toList();
        for (Comprobante c : afectados) {
            if (aceptado) {
                c.setEstadoSunat(EstadoSunat.ANULADO);
                c.setAnuladoAt(Instant.now());
            } else {
                c.setEstadoSunat(EstadoSunat.ACEPTADO);
                c.setSunatMensaje(recortar("SUNAT rechazó la anulación: " + mensaje));
            }
        }
    }

    // ------------------------------------------------------------------ apoyo

    private Credenciales credenciales(Empresa e) {
        if (e.getAmbienteSunat() == Empresa.Ambiente.BETA) {
            // En beta SUNAT acepta cualquier RUC con el usuario MODDATOS.
            return new Credenciales(e.getRuc() + "MODDATOS", "MODDATOS");
        }
        if (e.getSolUsuario() == null || e.getSolClaveEnc() == null) {
            throw ApiException.invalido("Falta tu usuario y clave SOL secundaria. Complétalos en Ajustes › SUNAT.");
        }
        return new Credenciales(e.getRuc() + e.getSolUsuario().toUpperCase(), cifrado.descifrar(e.getSolClaveEnc()));
    }

    private void guardarCdr(Comprobante c, byte[] zip) {
        ComprobanteArchivo cdr = archivos.findByComprobanteIdAndTipo(c.getId(), ComprobanteArchivo.Tipo.CDR).orElseGet(ComprobanteArchivo::new);
        cdr.setComprobante(c);
        cdr.setTipo(ComprobanteArchivo.Tipo.CDR);
        cdr.setNombre("R-" + c.getNombreArchivo() + ".zip");
        cdr.setContenido(zip);
        archivos.save(cdr);
    }

    private void registrar(Empresa e, Long comprobanteId, Long resumenId, Long bajaId, String operacion, boolean exito,
                           String codigo, String mensaje, String ticket, int ms) {
        EnvioSunat env = new EnvioSunat();
        env.setEmpresaId(e.getId());
        env.setComprobanteId(comprobanteId);
        env.setResumenId(resumenId);
        env.setBajaId(bajaId);
        env.setOperacion(operacion);
        env.setAmbiente(e.getAmbienteSunat().name());
        env.setExito(exito);
        env.setCodigo(codigo);
        env.setMensaje(recortar(mensaje));
        env.setTicket(ticket);
        env.setDuracionMs(ms);
        envios.save(env);
        if (!exito) log.info("SUNAT {} empresa={} comprobante={} código={} {}", operacion, e.getRuc(), comprobanteId, codigo, mensaje);
    }

    private static Long idResumen(LoteSunat l) {
        return l instanceof ResumenDiario ? l.getId() : null;
    }

    private static Long idBaja(LoteSunat l) {
        return l instanceof ComunicacionBaja ? l.getId() : null;
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
        return archivos.findByComprobanteIdAndTipo(comprobanteId, ComprobanteArchivo.Tipo.XML)
                .map(ComprobanteArchivo::getContenido).orElseThrow(() -> ApiException.noEncontrado("XML"));
    }

    public String xmlTexto(Long comprobanteId) {
        return new String(xmlDe(comprobanteId), StandardCharsets.UTF_8);
    }
}
