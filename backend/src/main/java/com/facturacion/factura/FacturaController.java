package com.facturacion.factura;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/facturas")
public class FacturaController {

    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    @GetMapping
    public List<FacturaResponse> listar(@AuthenticationPrincipal User user) {
        return facturaService.listar(user);
    }

    @PostMapping
    public FacturaResponse crear(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody FacturaRequest request) {
        return facturaService.crear(user, request);
    }

    @PatchMapping("/{id}/anular")
    public FacturaResponse anular(@AuthenticationPrincipal User user,
                                  @PathVariable("id") UUID id) {
        return facturaService.anular(user, id);
    }
}