package com.facturacion.info;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/info")
public class InfoController {

    @GetMapping
    public Map<String, Object> info() {
        return Map.of(
                "app", "Facturacion",
                "version", "v1",
                "mensaje", "API operativa"
        );
    }
}
