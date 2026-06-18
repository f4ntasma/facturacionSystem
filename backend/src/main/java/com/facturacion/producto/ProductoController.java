package com.facturacion.producto;

import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/productos")
    public List<ProductResponse> listar(@AuthenticationPrincipal User user,
                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                        @RequestParam(name = "size", defaultValue = "20") int size) {
        return productoService.listar(user, page, size);
    }

    @PostMapping("/productos")
    public ProductResponse crear(@AuthenticationPrincipal User user, @Valid @RequestBody ProductRequest request) {
        return productoService.crear(user, request);
    }

    @PutMapping("/productos/{id}")
    public ProductResponse actualizar(@AuthenticationPrincipal User user,
                                      @PathVariable("id") UUID id,
                                      @Valid @RequestBody ProductRequest request) {
        return productoService.actualizar(user, id, request);
    }

    @DeleteMapping("/productos/{id}")
    public void eliminar(@AuthenticationPrincipal User user, @PathVariable("id") UUID id) {
        productoService.eliminar(user, id);
    }

    @GetMapping("/productos/autocomplete")
    public List<ProductResponse> autocomplete(@AuthenticationPrincipal User user,
                                              @RequestParam(name = "q", defaultValue = "") String q,
                                              @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return productoService.autocomplete(user, q, limit);
    }

    @GetMapping("/catalogo")
    public List<ProductResponse> catalogoPublico(@RequestParam(name = "q", defaultValue = "") String q,
                                                 @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return productoService.catalogoPublico(q, limit);
    }
}
