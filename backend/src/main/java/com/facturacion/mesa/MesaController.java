package com.facturacion.mesa;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<MesaResponse> listar(@AuthenticationPrincipal User user) {
        return mesaService.listar(user);
    }

    @PostMapping
    public MesaResponse crear(@AuthenticationPrincipal User user,
                              @Valid @RequestBody MesaRequest request) {
        return mesaService.crear(user, request);
    }

    @PutMapping("/{id}")
    public MesaResponse actualizar(@AuthenticationPrincipal User user,
                                   @PathVariable UUID id,
                                   @Valid @RequestBody MesaRequest request) {
        return mesaService.actualizar(user, id, request);
    }

    @PatchMapping("/{id}/estado")
    public MesaResponse cambiarEstado(@AuthenticationPrincipal User user,
                                      @PathVariable UUID id,
                                      @RequestParam(name = "estado") EstadoMesa estado) {
        return mesaService.cambiarEstado(user, id, estado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@AuthenticationPrincipal User user,
                         @PathVariable UUID id) {
        mesaService.eliminar(user, id);
    }
}