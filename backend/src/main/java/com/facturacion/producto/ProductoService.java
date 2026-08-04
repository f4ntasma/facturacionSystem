package com.facturacion.producto;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<ProductResponse> listar(User user, int page, int size) {
        Empresa empresa = requireEmpresa(user);
        return productoRepository.findByEmpresa(empresa, PageRequest.of(page, size))
                .stream().map(ProductResponse::new).toList();
    }

    @Transactional
    public ProductResponse crear(User user, ProductRequest request) {
        Empresa empresa = requireEmpresa(user);
        if (request.getSku() != null && !request.getSku().isBlank() && productoRepository.existsBySkuAndEmpresa(request.getSku(), empresa)) {
            throw new BadRequestException("El SKU ya existe en tu empresa");
        }
        Producto p = new Producto();
        p.setEmpresa(empresa);
        copy(request, p);
        return new ProductResponse(productoRepository.save(p));
    }

    @Transactional
    public ProductResponse actualizar(User user, UUID id, ProductRequest request) {
        Empresa empresa = requireEmpresa(user);
        Producto producto = productoRepository.findById(id)
                .filter(p -> p.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        copy(request, producto);
        return new ProductResponse(productoRepository.save(producto));
    }

    @Transactional
    public void eliminar(User user, UUID id) {
        Empresa empresa = requireEmpresa(user);
        Producto producto = productoRepository.findById(id)
                .filter(p -> p.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    public List<ProductResponse> autocomplete(User user, String query, int limit) {
        Empresa empresa = requireEmpresa(user);
        return productoRepository.findByEmpresaAndNombreContainingIgnoreCase(
                        empresa,
                        query == null ? "" : query,
                        PageRequest.of(0, Math.max(limit, 1)))
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    public List<ProductResponse> catalogoPublico(String query, int limit) {
        return productoRepository.findByActivoTrueAndNombreContainingIgnoreCase(
                        query == null ? "" : query,
                        PageRequest.of(0, Math.max(limit, 1)))
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    private void copy(ProductRequest request, Producto producto) {
        producto.setNombre(request.getNombre());
        producto.setSku(request.getSku());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(request.getCategoria());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock() == null ? 0 : request.getStock());
    }

    private Empresa requireEmpresa(User user) {
        if (user.getEmpresa() == null) {
            throw new BadRequestException("El usuario no tiene empresa asociada");
        }
        return user.getEmpresa();
    }
}
