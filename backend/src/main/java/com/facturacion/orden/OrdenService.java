package com.facturacion.orden;

import com.facturacion.carrito.Carrito;
import com.facturacion.carrito.CarritoService;
import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.pago.EstadoPago;
import com.facturacion.pago.MetodoPago;
import com.facturacion.pago.Pago;
import com.facturacion.pago.PagoRepository;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenService {

    private static final BigDecimal IGV = new BigDecimal("0.18");

    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final CarritoService carritoService;

    public OrdenService(OrdenRepository ordenRepository, PagoRepository pagoRepository, CarritoService carritoService) {
        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
        this.carritoService = carritoService;
    }

    public List<OrdenResponse> listar(User user) {
        return ordenRepository.findByUser(user).stream()
                .map(OrdenResponse::new)
                .toList();
    }

    public OrdenResponse obtener(User user, UUID id) {
        Orden orden = findForUser(user, id);
        OrdenResponse response = new OrdenResponse(orden);
        pagoRepository.findByOrden(orden).ifPresent(pago -> response.setPago(new com.facturacion.pago.PagoResponse(pago)));
        return response;
    }

    @Transactional
    public OrdenResponse checkout(User user, CheckoutRequest request) {
        Carrito carrito = carritoService.getOrCreate(user);
        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }
        Empresa empresa = user.getEmpresa();
        if (empresa == null) {
            throw new BadRequestException("El usuario no tiene empresa asociada");
        }

        Orden orden = new Orden();
        orden.setEmpresa(empresa);
        orden.setUser(user);
        orden.setTipoComprobante(request.getTipoComprobante());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (var item : carrito.getItems()) {
            OrdenItem ordenItem = new OrdenItem();
            ordenItem.setOrden(orden);
            ordenItem.setProducto(item.getProducto());
            ordenItem.setCantidad(item.getCantidad());
            ordenItem.setPrecioUnitario(item.getPrecioUnitario());
            BigDecimal itemSubtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            ordenItem.setSubtotal(itemSubtotal);
            orden.getItems().add(ordenItem);
            subtotal = subtotal.add(itemSubtotal);

            // descontar stock disponible
            var producto = item.getProducto();
            if (producto.getStock() != null) {
                int restante = producto.getStock() - item.getCantidad();
                if (restante < 0) {
                    throw new BadRequestException("Stock insuficiente para " + producto.getNombre());
                }
                producto.setStock(restante);
            }
        }

        BigDecimal impuesto = subtotal.multiply(IGV).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto);
        orden.setSubtotal(subtotal);
        orden.setImpuesto(impuesto);
        orden.setTotal(total);

        Orden guardada = ordenRepository.save(orden);

        Pago pago = new Pago();
        pago.setOrden(guardada);
        pago.setMetodo(request.getMetodoPago());
        pago.setMonto(total);
        pago.setEstado(request.getMetodoPago() == MetodoPago.EFECTIVO ? EstadoPago.PENDIENTE : EstadoPago.PENDIENTE);
        Pago pagoGuardado = pagoRepository.save(pago);
        guardada.setPago(pagoGuardado);

        // vaciar carrito
        carrito.getItems().clear();

        OrdenResponse response = new OrdenResponse(guardada);
        response.setPago(new com.facturacion.pago.PagoResponse(pagoGuardado));
        return response;
    }

    @Transactional
    public OrdenResponse confirmarPago(User user, UUID ordenId, String referencia) {
        Orden orden = findForUser(user, ordenId);
        Pago pago = pagoRepository.findByOrden(orden)
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setReferencia(referencia);
        pagoRepository.save(pago);
        orden.setEstado(OrderStatus.PAGADA);
        ordenRepository.save(orden);

        OrdenResponse response = new OrdenResponse(orden);
        response.setPago(new com.facturacion.pago.PagoResponse(pago));
        return response;
    }

    private Orden findForUser(User user, UUID id) {
        return ordenRepository.findById(id)
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
    }

    @Transactional
    public void eliminar(User user, UUID id) {
        Orden orden = ordenRepository.findById(id)
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new NotFoundException("Orden no encontrada"));
        ordenRepository.delete(orden);
    }
}
