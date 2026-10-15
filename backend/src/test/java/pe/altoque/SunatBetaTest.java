package pe.altoque;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.altoque.sunat.SunatService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Envío real a SUNAT beta (e-beta.sunat.gob.pe). Solo corre con SUNAT_BETA=1:
 * {@code SUNAT_BETA=1 mvn test -Dtest=SunatBetaTest}
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "SUNAT_BETA", matches = "1")
class SunatBetaTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper json;
    @Autowired
    SunatService sunat;

    @Test
    void boletaFacturaNotaYBajaEnBeta() throws Exception {
        String token = llamar("/api/auth/registro", Map.of("ruc", "20000000001", "razonSocial", "EMPRESA DE PRUEBAS SAC",
                "direccion", "AV. PRUEBA 123", "nombre", "Beta", "email", "beta@altoque.pe", "password", "secreto123"), null).path("token").asText();

        JsonNode boleta = llamar("/api/comprobantes", Map.of("tipo", "03",
                "items", new Object[]{
                        Map.of("descripcion", "Gaseosa 1 L", "cantidad", 1, "precioUnitario", 5.50),
                        Map.of("descripcion", "Arroz extra 1 kg", "cantidad", 2, "precioUnitario", 4.20, "tipoAfectacion", "20", "unidadMedida", "KGM"),
                        Map.of("descripcion", "Bolsa plástica", "cantidad", 2, "precioUnitario", 0.10, "icbper", true)}), token);
        imprimir("BOLETA", boleta);
        assertThat(boleta.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        JsonNode boletaDni = llamar("/api/comprobantes", Map.of("tipo", "03",
                "cliente", Map.of("tipoDocumento", "1", "numeroDocumento", "45678912", "nombre", "MARIA HUAMAN ROJAS"),
                "items", new Object[]{Map.of("descripcion", "Leche evaporada", "cantidad", 3, "precioUnitario", 3.00)}), token);
        imprimir("BOLETA DNI", boletaDni);
        assertThat(boletaDni.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        JsonNode factura = llamar("/api/comprobantes", Map.of("tipo", "01",
                "cliente", Map.of("tipoDocumento", "6", "numeroDocumento", "20601234565", "nombre", "DISTRIBUIDORA EL CASERITO S.A.C.",
                        "direccion", "JR. HUALLAGA 456"),
                "items", new Object[]{Map.of("descripcion", "Caja de gaseosas x 12", "cantidad", 2, "precioUnitario", 59.00, "unidadMedida", "BX")}), token);
        imprimir("FACTURA", factura);
        assertThat(factura.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        JsonNode credito = llamar("/api/comprobantes", Map.of("tipo", "01",
                "cliente", Map.of("tipoDocumento", "6", "numeroDocumento", "20601234565", "nombre", "DISTRIBUIDORA EL CASERITO S.A.C."),
                "items", new Object[]{Map.of("descripcion", "Servicio de reparto", "cantidad", 1, "precioUnitario", 236.00, "unidadMedida", "ZZ")},
                "formaPago", "CREDITO",
                "cuotas", new Object[]{Map.of("monto", 236.00, "fechaVencimiento", java.time.LocalDate.now().plusDays(30).toString())}), token);
        imprimir("FACTURA CREDITO", credito);
        assertThat(credito.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        JsonNode nc = llamar("/api/comprobantes/" + factura.path("id").asLong() + "/notas", Map.of("tipo", "07", "motivoCodigo", "01",
                "motivoDescripcion", "Anulación de la operación"), token);
        imprimir("NOTA CREDITO", nc);
        assertThat(nc.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        JsonNode nd = llamar("/api/comprobantes/" + credito.path("id").asLong() + "/notas", Map.of("tipo", "08", "motivoCodigo", "01",
                "motivoDescripcion", "Intereses por mora",
                "items", new Object[]{Map.of("descripcion", "Intereses por mora", "cantidad", 1, "precioUnitario", 11.80, "unidadMedida", "ZZ")}), token);
        imprimir("NOTA DEBITO", nd);
        assertThat(nd.path("estadoSunat").asText()).isIn("ACEPTADO", "OBSERVADO");

        // Anulación de la boleta (resumen diario) y de la factura a crédito (comunicación de baja)
        JsonNode anulaBoleta = llamar("/api/comprobantes/" + boletaDni.path("id").asLong() + "/anular", Map.of("motivo", "Error en el monto"), token);
        imprimir("ANULA BOLETA", anulaBoleta);
        JsonNode anulaFactura = llamar("/api/comprobantes/" + nd.path("id").asLong() + "/anular", Map.of("motivo", "Error en el monto"), token);
        imprimir("ANULA ND", anulaFactura);
        Thread.sleep(6000);
        JsonNode lotes = json.readTree(mvc.perform(get("/api/lotes").header("Authorization", "Bearer " + token)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8));
        for (JsonNode l : lotes) {
            llamar("/api/lotes/" + l.path("clase").asText() + "/" + l.path("id").asLong() + "/consultar", Map.of(), token);
        }
        lotes = json.readTree(mvc.perform(get("/api/lotes").header("Authorization", "Bearer " + token)).andReturn().getResponse()
                .getContentAsString(StandardCharsets.UTF_8));
        System.out.println("LOTES → " + lotes.toPrettyString());
        for (JsonNode l : lotes) assertThat(l.path("estado").asText()).as(l.toString()).isIn("ACEPTADO", "ENVIADO");
    }

    private void imprimir(String que, JsonNode c) {
        System.out.printf("%s %s → %s [%s] %s%n", que, c.path("numero").asText(), c.path("estadoSunat").asText(),
                c.path("sunatCodigo").asText(), c.path("sunatMensaje").asText());
    }

    private JsonNode llamar(String url, Object cuerpo, String token) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsBytes(cuerpo));
        if (token != null) req.header("Authorization", "Bearer " + token);
        var res = mvc.perform(req).andReturn().getResponse();
        String body = res.getContentAsString(StandardCharsets.UTF_8);
        assertThat(res.getStatus()).as(url + " → " + body).isEqualTo(200);
        return body.isBlank() ? json.createObjectNode() : json.readTree(body);
    }
}
