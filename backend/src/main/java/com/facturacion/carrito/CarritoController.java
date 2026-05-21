package com.facturacion.carrito;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    public CarritoResponse getCarrito(@AuthenticationPrincipal User user) {
        return new CarritoResponse(carritoService.getOrCreate(user));
    }

    @PostMapping("/items")
    public CarritoResponse addItem(@AuthenticationPrincipal User user,
                                   @Valid @RequestBody CarritoItemRequest request) {
        return new CarritoResponse(carritoService.addItem(user, request));
    }

    @PutMapping("/items/{itemId}")
    public CarritoResponse updateItem(@AuthenticationPrincipal User user,
                                      @PathVariable UUID itemId,
                                      @RequestParam int cantidad) {
        return new CarritoResponse(carritoService.updateCantidad(user, itemId, cantidad));
    }

    @DeleteMapping("/items/{itemId}")
    public CarritoResponse removeItem(@AuthenticationPrincipal User user,
                                      @PathVariable UUID itemId) {
        return new CarritoResponse(carritoService.removeItem(user, itemId));
    }

    @DeleteMapping
    public void limpiarCarrito(@AuthenticationPrincipal User user) {
        carritoService.limpiarCarrito(user);
    }

}
