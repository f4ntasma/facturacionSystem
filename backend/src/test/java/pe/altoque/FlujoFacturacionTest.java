package pe.altoque;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import pe.altoque.empresa.Empresa;
import pe.altoque.empresa.EmpresaRepository;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Flujo de punta a punta en modo SIMULADO: registro → catálogo → boleta → factura → nota → cobro → anulación → reportes. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FlujoFacturacionTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    EmpresaRepository empresas;

    String token;

    @BeforeEach
    void registrar() throws Exception {
        String ruc = "20509998885";
        if (empresas.existsByRuc(ruc)) {
            token = post("/api/auth/login", Map.of("email", "dueno@bodega.pe", "password", "secreto123"), null).path("token").asText();
            return;
        }
        JsonNode r = post("/api/auth/registro", Map.of(
                "ruc", ruc, "razonSocial", "Bodega La Esquina SAC", "direccion", "Av. Perú 100", "nombre", "Rosa",
                "email", "dueno@bodega.pe", "password", "secreto123"), null);
        token = r.path("token").asText();
        assertThat(r.path("perfil").path("empresa").path("ruc").asText()).isEqualTo(ruc);
        Empresa e = empresas.findByRuc(ruc).orElseThrow();
        e.setAmbienteSunat(Empresa.Ambiente.SIMULADO);
        empresas.save(e);
    }

    @Test
    @Order(1)
    void flujoCompleto() throws Exception {
        JsonNode gaseosa = post("/api/productos", Map.of("nombre", "Gaseosa 1 L", "unidadMedida", "NIU", "precio", 5.50,
                "tipoAfectacion", "10", "controlaStock", true, "stock", 10, "categoria", "Bebidas"), token);
        long gaseosaId = gaseosa.path("id").asLong();
        assertThat(gaseosa.path("codigo").asText()).startsWith("P");

        // Boleta sin cliente (cliente varios), pagada con Yape
        JsonNode boleta = post("/api/comprobantes", Map.of(
                "tipo", "03",
                "items", new Object[]{
                        Map.of("productoId", gaseosaId, "descripcion", "Gaseosa 1 L", "cantidad", 2, "precioUnitario", 5.50),
                        Map.of("descripcion", "Pan francés", "cantidad", 10, "precioUnitario", 0.40)},
                "pagos", new Object[]{Map.of("metodo", "YAPE", "monto", 15.00)}), token);
        assertThat(boleta.path("numero").asText()).isEqualTo("B001-000001");
        assertThat(boleta.path("total").decimalValue()).isEqualByComparingTo("15.00");
        assertThat(boleta.path("estadoSunat").asText()).isEqualTo("ACEPTADO");
        assertThat(boleta.path("totalLetras").asText()).isEqualTo("QUINCE CON 00/100 SOLES");
        assertThat(boleta.path("clienteNombre").asText()).isEqualTo("CLIENTES VARIOS");
        assertThat(boleta.path("hash").asText()).isNotBlank();
        assertThat(obtener("/api/productos/" + gaseosaId).path("stock").decimalValue()).isEqualByComparingTo("8");

        // El XML está firmado y la firma es válida
        verificarFirma(mvc.perform(get("/api/comprobantes/" + boleta.path("id").asLong() + "/xml").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());

        // Enlace público y QR
        String tokenPublico = boleta.path("tokenPublico").asText();
        mvc.perform(get("/api/public/c/" + tokenPublico)).andExpect(status().isOk());
        mvc.perform(get("/api/public/c/" + tokenPublico + "/qr.svg")).andExpect(status().isOk());

        // Factura sin RUC: rechazo con mensaje en tono de marca
        JsonNode error = postEsperando("/api/comprobantes", Map.of("tipo", "01",
                "items", new Object[]{Map.of("descripcion", "Servicio", "cantidad", 1, "precioUnitario", 100)}), token, 400);
        assertThat(error.path("mensaje").asText()).contains("Falta el RUC, pe");

        // Factura al crédito con dos cuotas y cliente guardado
        String hoy = java.time.LocalDate.now().toString();
        JsonNode factura = post("/api/comprobantes", Map.of(
                "tipo", "01",
                "cliente", Map.of("tipoDocumento", "6", "numeroDocumento", "20601234565", "nombre", "DISTRIBUIDORA EL CASERITO S.A.C.",
                        "direccion", "Jr. Huallaga 456", "guardar", true),
                "items", new Object[]{Map.of("descripcion", "Caja de gaseosas x 12", "cantidad", 2, "precioUnitario", 59.00, "unidadMedida", "BX")},
                "formaPago", "CREDITO",
                "cuotas", new Object[]{
                        Map.of("monto", 59.00, "fechaVencimiento", java.time.LocalDate.now().plusDays(15).toString()),
                        Map.of("monto", 59.00, "fechaVencimiento", java.time.LocalDate.now().plusDays(30).toString())}), token);
        assertThat(factura.path("numero").asText()).isEqualTo("F001-000001");
        assertThat(factura.path("totalIgv").decimalValue()).isEqualByComparingTo("18.00");
        assertThat(factura.path("estadoPago").asText()).isEqualTo("POR_COBRAR");
        assertThat(factura.path("cuotas").size()).isEqualTo(2);
        assertThat(obtener("/api/clientes?q=caserito").path("totalElements").asInt()).isEqualTo(1);

        // Cobro parcial
        long facturaId = factura.path("id").asLong();
        JsonNode cobrada = post("/api/comprobantes/" + facturaId + "/pagos", Map.of("metodo", "TRANSFERENCIA", "monto", 59.00), token);
        assertThat(cobrada.path("estadoPago").asText()).isEqualTo("PARCIAL");
        assertThat(cobrada.path("saldo").decimalValue()).isEqualByComparingTo("59.00");

        // Nota de crédito por devolución de un ítem (parcial)
        JsonNode nc = post("/api/comprobantes/" + facturaId + "/notas", Map.of(
                "tipo", "07", "motivoCodigo", "07", "motivoDescripcion", "Devolvió una caja",
                "items", new Object[]{Map.of("descripcion", "Caja de gaseosas x 12", "cantidad", 1, "precioUnitario", 59.00, "unidadMedida", "BX")}), token);
        assertThat(nc.path("numero").asText()).isEqualTo("FC01-000001");
        assertThat(nc.path("referencia").path("numero").asText()).isEqualTo("F001-000001");
        assertThat(nc.path("total").decimalValue()).isEqualByComparingTo("59.00");
        verificarFirma(mvc.perform(get("/api/comprobantes/" + nc.path("id").asLong() + "/xml").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsByteArray());

        // No se puede acreditar más que el total
        JsonNode exceso = postEsperando("/api/comprobantes/" + facturaId + "/notas", Map.of(
                "tipo", "07", "motivoCodigo", "09", "motivoDescripcion", "Rebaja",
                "items", new Object[]{Map.of("descripcion", "Rebaja", "cantidad", 1, "precioUnitario", 80.00)}), token, 400);
        assertThat(exceso.path("mensaje").asText()).contains("supera");

        // Anular la boleta (resumen diario simulado) repone stock
        JsonNode anulada = post("/api/comprobantes/" + boleta.path("id").asLong() + "/anular", Map.of("motivo", "Error de digitación"), token);
        assertThat(anulada.path("estadoSunat").asText()).isEqualTo("ANULADO");
        assertThat(obtener("/api/productos/" + gaseosaId).path("stock").decimalValue()).isEqualByComparingTo("10");
        assertThat(obtener("/api/lotes").get(0).path("identificador").asText()).startsWith("RC-");

        // Reportes: la boleta anulada no cuenta; factura 118 − NC 59 = 59
        JsonNode resumen = obtener("/api/reportes/resumen");
        assertThat(resumen.path("ventasHoy").decimalValue()).isEqualByComparingTo("59.00");
        assertThat(resumen.path("comprobantesHoy").asLong()).isEqualTo(1);

        // Listado con filtros y exportación
        assertThat(obtener("/api/comprobantes?tipo=01,03").path("totalElements").asInt()).isEqualTo(2);
        assertThat(obtener("/api/comprobantes?q=F001-1").path("totalElements").asInt()).isEqualTo(1);
        String csv = mvc.perform(get("/api/comprobantes/exportar?desde=" + hoy + "&hasta=" + hoy).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("F001;1").contains("FC01;1").contains("-59.00");

        // Sin token no hay acceso
        mvc.perform(get("/api/comprobantes")).andExpect(status().isUnauthorized());
    }

    @Test
    void cajaCuadraEfectivo() throws Exception {
        post("/api/caja/abrir", Map.of("montoApertura", 50), token);
        post("/api/comprobantes", Map.of("tipo", "03",
                "items", new Object[]{Map.of("descripcion", "Leche", "cantidad", 2, "precioUnitario", 3.00)}), token);
        JsonNode cerrada = post("/api/caja/cerrar", Map.of("montoDeclarado", 55), token);
        assertThat(cerrada.path("montoEsperado").decimalValue()).isEqualByComparingTo("56.00");
        assertThat(cerrada.path("diferencia").decimalValue()).isEqualByComparingTo("-1.00");
    }

    @Test
    void facturaConDetraccionAlCredito() throws Exception {
        // Sin cuenta del Banco de la Nación no se puede
        Map<String, Object> pedido = Map.of(
                "tipo", "01",
                "cliente", Map.of("tipoDocumento", "6", "numeroDocumento", "20601234565", "nombre", "DISTRIBUIDORA EL CASERITO S.A.C."),
                "items", new Object[]{Map.of("descripcion", "Mantenimiento de congeladora", "cantidad", 1, "precioUnitario", 1000.00,
                        "unidadMedida", "ZZ")},
                "detraccion", Map.of("codigo", "020"),
                "formaPago", "CREDITO",
                "cuotas", new Object[]{Map.of("monto", 880.00, "fechaVencimiento", java.time.LocalDate.now().plusDays(30).toString())});
        assertThat(postEsperando("/api/comprobantes", pedido, token, 400).path("mensaje").asText()).contains("Banco de la Nación");

        put("/api/empresa/sunat", Map.of("ambiente", "SIMULADO", "cuentaDetracciones", "00012345678"));
        JsonNode f = post("/api/comprobantes", pedido, token);
        // 12 % de S/ 1,000 = S/ 120; el cliente paga S/ 880 al emisor
        assertThat(f.path("detraccion").path("monto").decimalValue()).isEqualByComparingTo("120.00");
        assertThat(f.path("netoACobrar").decimalValue()).isEqualByComparingTo("880.00");
        assertThat(f.path("saldo").decimalValue()).isEqualByComparingTo("880.00");
        String xml = new String(mvc.perform(get("/api/comprobantes/" + f.path("id").asLong() + "/xml").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsByteArray(), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(xml).contains("listID=\"1001\"").contains("languageLocaleID=\"2006\"").contains("<cbc:PaymentMeansID>020</cbc:PaymentMeansID>")
                .contains("<cbc:PaymentPercent>12</cbc:PaymentPercent>").contains("00012345678");
    }

    @Test
    void kardexYVariosNegocios() throws Exception {
        JsonNode arroz = post("/api/productos", Map.of("nombre", "Arroz 5 kg", "unidadMedida", "NIU", "precio", 22.00,
                "tipoAfectacion", "20", "controlaStock", true, "stock", 5), token);
        long id = arroz.path("id").asLong();
        post("/api/productos/" + id + "/ingresos", Map.of("cantidad", 3, "nota", "Compra al mayorista"), token);
        post("/api/comprobantes", Map.of("tipo", "03",
                "items", new Object[]{Map.of("productoId", id, "descripcion", "Arroz 5 kg", "cantidad", 2, "precioUnitario", 22.00)}), token);
        JsonNode kardex = obtener("/api/productos/" + id + "/kardex");
        assertThat(kardex.size()).isEqualTo(3);
        assertThat(kardex.get(0).path("tipo").asText()).isEqualTo("VENTA");
        assertThat(kardex.get(0).path("stockResultante").decimalValue()).isEqualByComparingTo("6");

        // La misma persona suma otro negocio y entra a él con otro token
        JsonNode otro = post("/api/auth/negocios", Map.of("ruc", "10456789019", "razonSocial", "Taller El Chino",
                "direccion", "Jr. Lima 200", "distrito", "MIRAFLORES", "provincia", "LIMA", "departamento", "LIMA"), token);
        assertThat(otro.path("perfil").path("empresa").path("ruc").asText()).isEqualTo("10456789019");
        assertThat(otro.path("perfil").path("negocios").asInt()).isGreaterThanOrEqualTo(2);
        String tokenOtro = otro.path("token").asText();
        assertThat(leer(mvc.perform(get("/api/auth/negocios").header("Authorization", "Bearer " + tokenOtro))).size()).isGreaterThanOrEqualTo(2);
        // Los comprobantes de un negocio no se ven desde el otro
        assertThat(leer(mvc.perform(get("/api/comprobantes").header("Authorization", "Bearer " + tokenOtro))).path("totalElements").asInt())
                .isZero();
        assertThat(leer(mvc.perform(get("/api/empresa").header("Authorization", "Bearer " + tokenOtro))).path("distrito").asText())
                .isEqualTo("MIRAFLORES");
    }

    // ------------------------------------------------------------------ apoyo

    private void put(String url, Object cuerpo) throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(cuerpo))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private static void verificarFirma(byte[] xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
        NodeList firmas = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        assertThat(firmas.getLength()).isEqualTo(1);
        String b64 = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "X509Certificate").item(0).getTextContent().replaceAll("\\s", "");
        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(b64)));
        DOMValidateContext ctx = new DOMValidateContext(cert.getPublicKey(), firmas.item(0));
        ctx.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
        XMLSignature firma = XMLSignatureFactory.getInstance("DOM").unmarshalXMLSignature(ctx);
        assertThat(firma.validate(ctx)).as("firma XML válida").isTrue();
    }

    private JsonNode obtener(String url) throws Exception {
        return leer(mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(url)
                .header("Authorization", "Bearer " + token)).andExpect(status().isOk()));
    }

    private JsonNode post(String url, Object cuerpo, String tk) throws Exception {
        return postEsperando(url, cuerpo, tk, 200);
    }

    private JsonNode postEsperando(String url, Object cuerpo, String tk, int esperado) throws Exception {
        var req = org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(cuerpo));
        if (tk != null) req.header("Authorization", "Bearer " + tk);
        ResultActions r = mvc.perform(req);
        int st = r.andReturn().getResponse().getStatus();
        String body = r.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertThat(st).as("HTTP %s %s → %s", url, st, body).isEqualTo(esperado);
        return body.isBlank() ? json.createObjectNode() : json.readTree(body);
    }

    private JsonNode leer(ResultActions r) throws Exception {
        return json.readTree(r.andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8));
    }
}
