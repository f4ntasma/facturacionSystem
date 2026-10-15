package pe.altoque.catalogo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.altoque.common.ApiException;
import pe.altoque.common.Catalogos;
import pe.altoque.config.AltoqueProperties;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Autocompletar nombre por DNI y razón social por RUC (apiperu.dev). */
@RestController
@RequestMapping("/api/consulta")
public class ConsultaController {

    public record Persona(String dni, String nombre) {}

    public record Contribuyente(String ruc, String razonSocial, String direccion, String estado, String condicion,
                                String departamento, String provincia, String distrito, String ubigeo) {}

    private final AltoqueProperties props;
    private final ObjectMapper json;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public ConsultaController(AltoqueProperties props, ObjectMapper json) {
        this.props = props;
        this.json = json;
    }

    @GetMapping("/dni/{dni}")
    public Persona dni(@PathVariable String dni) {
        if (!dni.matches("\\d{8}")) throw ApiException.invalido("El DNI tiene 8 dígitos.");
        return (Persona) cache.computeIfAbsent("dni:" + dni, k -> {
            JsonNode d = consultar("/dni/" + dni);
            String nombre = texto(d, "nombre_completo");
            if (nombre == null) nombre = (texto(d, "nombres") + " " + texto(d, "apellido_paterno") + " " + texto(d, "apellido_materno")).trim();
            return new Persona(dni, nombre);
        });
    }

    @GetMapping("/ruc/{ruc}")
    public Contribuyente ruc(@PathVariable String ruc) {
        if (!Catalogos.rucValido(ruc)) throw ApiException.invalido("El RUC no es válido, pe.");
        return (Contribuyente) cache.computeIfAbsent("ruc:" + ruc, k -> {
            JsonNode d = consultar("/ruc/" + ruc);
            return new Contribuyente(ruc, texto(d, "nombre_o_razon_social"),
                    texto(d, "direccion") != null ? texto(d, "direccion") : texto(d, "direccion_completa"),
                    texto(d, "estado"), texto(d, "condicion"), texto(d, "departamento"), texto(d, "provincia"),
                    texto(d, "distrito"), texto(d, "ubigeo_sunat"));
        });
    }

    private JsonNode consultar(String ruta) {
        String token = props.apiperuToken();
        if (token == null || token.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El autocompletado no está configurado; escribe los datos a mano.");
        }
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://apiperu.dev/api" + ruta))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode raiz = json.readTree(res.body());
            if (!raiz.path("success").asBoolean(false) || !raiz.has("data")) {
                throw ApiException.noEncontrado("Documento");
            }
            return raiz.get("data");
        } catch (ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "La consulta tardó mucho. Escribe los datos a mano.");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "No pudimos consultar ahora. Escribe los datos a mano.");
        }
    }

    private static String texto(JsonNode n, String campo) {
        JsonNode v = n.get(campo);
        return v == null || v.isNull() || v.asText().isBlank() ? null : v.asText().trim();
    }
}
