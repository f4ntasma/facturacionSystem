package com.facturacion.empresa;

import com.facturacion.common.NotFoundException;
import com.facturacion.user.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresaController {

    private final EmpresaRepository empresaRepository;

    public EmpresaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public List<EmpresaResponse> listar(@AuthenticationPrincipal User user) {
        if (user.getEmpresa() == null) return List.of();
        return List.of(new EmpresaResponse(user.getEmpresa()));
    }

    @DeleteMapping("/{id}")
    public void eliminar(@AuthenticationPrincipal User user, @PathVariable UUID id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada"));
        empresaRepository.delete(empresa);
    }

    @GetMapping("/{ruc}")
    public EmpresaResponse obtenerPorRuc(@PathVariable String ruc) {
        Empresa empresa = empresaRepository.findByRuc(ruc)
                .orElseThrow(() -> new NotFoundException("Empresa no encontrada"));
        return new EmpresaResponse(empresa);
    }

    @PostMapping
    public EmpresaResponse crearOActualizar(@AuthenticationPrincipal User user,
                                            @Valid @RequestBody EmpresaRequest request) {
        Empresa empresa = user.getEmpresa() != null ? user.getEmpresa() : new Empresa();
        empresa.setRuc(request.getRuc());
        empresa.setRazonSocial(request.getRazonSocial());
        empresa.setDireccion(request.getDireccion());
        empresa.setTelefono(request.getTelefono());
        empresa.setOwner(user);
        Empresa guardada = empresaRepository.save(empresa);
        user.setEmpresa(guardada);
        return new EmpresaResponse(guardada);
    }

}
