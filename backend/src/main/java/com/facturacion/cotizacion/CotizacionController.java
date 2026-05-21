package com.facturacion.cotizacion;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @GetMapping
    public List<CotizacionResponse> listar(@AuthenticationPrincipal User user) {
        return cotizacionService.listar(user);
    }

    @PostMapping
    public CotizacionResponse crear(@AuthenticationPrincipal User user,
                                    @Valid @RequestBody CotizacionRequest request) {
        return cotizacionService.crear(user, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        cotizacionService.eliminar(user, id);
    }
}