package pe.altoque.sunat;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import pe.altoque.config.AltoqueProperties;
import pe.altoque.empresa.Empresa;
import pe.altoque.sunat.xml.Xml;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Cliente SOAP de los servicios billService de SUNAT (sendBill, sendSummary, getStatus). */
@Component
public class SunatClient {

    /** Resultado de una llamada: o trae CDR/ticket, o una falla (código SUNAT o error de red). */
    public record Respuesta(boolean ok, byte[] cdrZip, String ticket, String statusCode, String faultCodigo, String faultMensaje,
                            boolean errorRed) {

        static Respuesta falla(String codigo, String mensaje) {
            return new Respuesta(false, null, null, null, codigo, mensaje, false);
        }

        static Respuesta red(String mensaje) {
            return new Respuesta(false, null, null, null, null, mensaje, true);
        }
    }

    public record Credenciales(String usuario, String clave) {}

    private static final Pattern CODIGO = Pattern.compile("(\\d{4})");

    private final HttpClient http;
    private final AltoqueProperties props;

    public SunatClient(AltoqueProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String url(Empresa.Ambiente ambiente) {
        return ambiente == Empresa.Ambiente.PRODUCCION ? props.sunat().produccionFacturaUrl() : props.sunat().betaFacturaUrl();
    }

    public Respuesta sendBill(Empresa.Ambiente ambiente, Credenciales cred, String nombreZip, byte[] zip) {
        String cuerpo = "<ser:sendBill><fileName>" + nombreZip + "</fileName><contentFile>"
                + Base64.getEncoder().encodeToString(zip) + "</contentFile></ser:sendBill>";
        return llamar(ambiente, cred, cuerpo);
    }

    public Respuesta sendSummary(Empresa.Ambiente ambiente, Credenciales cred, String nombreZip, byte[] zip) {
        String cuerpo = "<ser:sendSummary><fileName>" + nombreZip + "</fileName><contentFile>"
                + Base64.getEncoder().encodeToString(zip) + "</contentFile></ser:sendSummary>";
        return llamar(ambiente, cred, cuerpo);
    }

    public Respuesta getStatus(Empresa.Ambiente ambiente, Credenciales cred, String ticket) {
        return llamar(ambiente, cred, "<ser:getStatus><ticket>" + Xml.escapar(ticket) + "</ticket></ser:getStatus>");
    }

    private Respuesta llamar(Empresa.Ambiente ambiente, Credenciales cred, String cuerpo) {
        String sobre = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" \
                xmlns:ser="http://service.sunat.gob.pe" \
                xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">\
                <soapenv:Header><wsse:Security><wsse:UsernameToken>\
                <wsse:Username>%s</wsse:Username><wsse:Password>%s</wsse:Password>\
                </wsse:UsernameToken></wsse:Security></soapenv:Header>\
                <soapenv:Body>%s</soapenv:Body></soapenv:Envelope>"""
                .formatted(Xml.escapar(cred.usuario()), Xml.escapar(cred.clave()), cuerpo);
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url(ambiente)))
                    .timeout(Duration.ofSeconds(props.sunat().timeoutSegundos()))
                    .header("Content-Type", "text/xml; charset=utf-8")
                    .header("SOAPAction", "")
                    .POST(HttpRequest.BodyPublishers.ofString(sobre, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
            return interpretar(res.statusCode(), res.body());
        } catch (java.io.IOException e) {
            return Respuesta.red("No hubo conexión con SUNAT: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Respuesta.red("Envío interrumpido");
        }
    }

    static Respuesta interpretar(int status, byte[] cuerpo) {
        Document doc;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(cuerpo));
        } catch (Exception e) {
            return Respuesta.red("SUNAT respondió algo ilegible (HTTP " + status + ")");
        }
        String faultCode = CdrLector.texto(doc, "faultcode");
        if (faultCode != null) {
            String faultString = CdrLector.texto(doc, "faultstring");
            String codigo = extraerCodigo(faultCode);
            if (codigo == null) codigo = extraerCodigo(faultString);
            String detalle = CdrLector.texto(doc, "message");
            String mensaje = faultString != null ? faultString : faultCode;
            if (detalle != null && !detalle.equals(mensaje)) mensaje += " · " + detalle;
            return Respuesta.falla(codigo, mensaje);
        }
        String app = CdrLector.texto(doc, "applicationResponse");
        if (app != null) return new Respuesta(true, Base64.getMimeDecoder().decode(app), null, null, null, null, false);
        String ticket = CdrLector.texto(doc, "ticket");
        if (ticket != null) return new Respuesta(true, null, ticket, null, null, null, false);
        String statusCode = CdrLector.texto(doc, "statusCode");
        if (statusCode != null) {
            String content = CdrLector.texto(doc, "content");
            byte[] zip = content != null && !content.isBlank() ? Base64.getMimeDecoder().decode(content) : null;
            return new Respuesta(true, zip, null, statusCode, null, null, false);
        }
        return Respuesta.red("Respuesta de SUNAT sin contenido (HTTP " + status + ")");
    }

    private static String extraerCodigo(String s) {
        if (s == null) return null;
        Matcher m = CODIGO.matcher(s);
        return m.find() ? m.group(1) : null;
    }
}
