package com.facturacion.sunat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ApiPeruService {

    @Value("${apiperu.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://apiperu.dev/api";

    public Map<String, Object> consultarDni(String dni) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("dni", dni);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/dni", HttpMethod.POST, entity, Map.class
        );
        return response.getBody();
    }

    public Map<String, Object> consultarRuc(String ruc) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of("ruc", ruc);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                BASE_URL + "/ruc", HttpMethod.POST, entity, Map.class
        );
        return response.getBody();
    }
}
