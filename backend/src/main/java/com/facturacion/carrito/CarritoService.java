package com.facturacion.carrito;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.producto.Producto;
import com.facturacion.producto.ProductoRepository;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;

    public CarritoService(CarritoRepository carritoRepository, ProductoRepository productoRepository) {
        this.carritoRepository = carritoRepository;
        this.productoRepository = productoRepository;
    }

    public Carrito getOrCreate(User user) {
        return carritoRepository.findByUser(user).orElseGet(() -> {
            Carrito c = new Carrito();
            c.setUser(user);
            return carritoRepository.save(c);
        });
    }

    @Transactional
    public Carrito addItem(User user, CarritoItemRequest request) {
        Carrito carrito = getOrCreate(user);
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));
        if (producto.getEmpresa() != null && user.getEmpresa() != null
                && !producto.getEmpresa().getId().equals(user.getEmpresa().getId())) {
            throw new BadRequestException("El producto no pertenece a tu empresa");
        }
        if (producto.getStock() != null && request.getCantidad() > producto.getStock()) {
            throw new BadRequestException("Stock insuficiente");
        }

        CarritoItem item = carrito.getItems().stream()
                .filter(it -> it.getProducto().getId().equals(producto.getId()))
                .findFirst()
                .orElseGet(() -> {
                    CarritoItem nuevo = new CarritoItem();
                    nuevo.setCarrito(carrito);
                    nuevo.setProducto(producto);
                    nuevo.setCantidad(0);
                    nuevo.setPrecioUnitario(producto.getPrecio());
                    carrito.getItems().add(nuevo);
                    return nuevo;
                });

        item.setCantidad(item.getCantidad() + request.getCantidad());
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito updateCantidad(User user, UUID itemId, int cantidad) {
        Carrito carrito = getOrCreate(user);
        CarritoItem item = carrito.getItems().stream()
                .filter(it -> it.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item no encontrado en el carrito"));
        if (cantidad <= 0) {
            carrito.getItems().remove(item);
        } else {
            if (item.getProducto().getStock() != null && cantidad > item.getProducto().getStock()) {
                throw new BadRequestException("Stock insuficiente");
            }
            item.setCantidad(cantidad);
        }
        return carritoRepository.save(carrito);
    }

    @Transactional
    public Carrito removeItem(User user, UUID itemId) {
        Carrito carrito = getOrCreate(user);
        carrito.getItems().removeIf(it -> it.getId().equals(itemId));
        return carritoRepository.save(carrito);
    }

    @Transactional
    public void limpiarCarrito(User user) {
        Carrito carrito = getOrCreate(user);
        carrito.getItems().clear();
        carritoRepository.save(carrito);
    }

}
