package com.facturacion.cliente;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteResponse> listar(@AuthenticationPrincipal User user) {
        return clienteService.listar(user);
    }

    @PostMapping
    public ClienteResponse crear(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody ClienteRequest request) {
        return clienteService.crear(user, request);
    }

    @PutMapping("/{id}")
    public ClienteResponse actualizar(@AuthenticationPrincipal User user,
                                      @PathVariable UUID id,
                                      @Valid @RequestBody ClienteRequest request) {
        return clienteService.actualizar(user, id, request);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        clienteService.eliminar(user, id);
    }
}