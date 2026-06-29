package com.facturacion.orden;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping("/ordenes")
    public List<OrdenResponse> listar(@AuthenticationPrincipal User user) {
        return ordenService.listar(user);
    }

    @GetMapping("/ordenes/{id}")
    public OrdenResponse obtener(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        return ordenService.obtener(user, id);
    }

    @PostMapping("/checkout")
    public OrdenResponse checkout(@AuthenticationPrincipal User user,
                                  @Valid @RequestBody CheckoutRequest request) {
        return ordenService.checkout(user, request);
    }

    @PostMapping("/ordenes/{id}/confirmar")
    public OrdenResponse confirmar(@AuthenticationPrincipal User user,
                                   @PathVariable("id") UUID id,
                                   @RequestParam(required = false) String referencia) {
        return ordenService.confirmarPago(user, id, referencia);
    }

    @DeleteMapping("/ordenes/{id}")
    public void eliminar(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        ordenService.eliminar(user, id);
    }
}
