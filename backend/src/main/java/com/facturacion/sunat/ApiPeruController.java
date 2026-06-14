package com.facturacion.sunat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/consulta")
public class ApiPeruController {

    private final ApiPeruService apiPeruService;

    public ApiPeruController(ApiPeruService apiPeruService) {
        this.apiPeruService = apiPeruService;
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<Map<String, Object>> consultarDni(@PathVariable("dni") String dni) {
        return ResponseEntity.ok(apiPeruService.consultarDni(dni));
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<Map<String, Object>> consultarRuc(@PathVariable("ruc") String ruc) {
        return ResponseEntity.ok(apiPeruService.consultarRuc(ruc));
    }

}