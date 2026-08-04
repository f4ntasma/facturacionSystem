package com.facturacion.orden;

import com.facturacion.carrito.Carrito;
import com.facturacion.carrito.CarritoService;
import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.common.TipoComprobante;
import com.facturacion.empresa.Empresa;
import com.facturacion.factura.FacturaRequest;
import com.facturacion.factura.FacturaService;
import com.facturacion.pago.EstadoPago;
import com.facturacion.pago.Pago;
import com.facturacion.pago.PagoRepository;
import com.facturacion.user.User;
import com.facturacion.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrdenService {

    private static final BigDecimal IGV = new BigDecimal("0.18");

    private final OrdenRepository ordenRepository;
    private final PagoRepository pagoRepository;
    private final CarritoService carritoService;
    private final UserRepository userRepository;
    private final FacturaService facturaService;

    public OrdenService(OrdenRepository ordenRepository, PagoRepository pagoRepository,
                        CarritoService carritoService, UserRepository userRepository,
                        FacturaService facturaService) {
        this.ordenRepository = ordenRepository;
        this.pagoRepository = pagoRepository;
        this.carritoService = carritoService;
        this.userRepository = userRepository;
        this.facturaService = facturaService;
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
        // Recargar el usuario desde BD para que Empresa sea una entidad gestionada.
        // El User inyectado por @AuthenticationPrincipal llega DETACHED (fuera de sesión JPA)
        // y causaría "detached entity passed to persist" al asignarlo a Orden.
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        Carrito carrito = carritoService.getOrCreate(managedUser);
        if (carrito.getItems().isEmpty()) {
            throw new BadRequestException("El carrito está vacío");
        }
        Empresa empresa = managedUser.getEmpresa();
        if (empresa == null) {
            throw new BadRequestException("El usuario no tiene empresa asociada");
        }

        // Validar antes de crear la orden: FACTURA requiere RUC valido del cliente
        if (request.getTipoComprobante() == TipoComprobante.FACTURA) {
            String ruc = request.getClienteRuc();
            if (ruc == null || !ruc.matches("\\d{11}")) {
                throw new BadRequestException("Para emitir factura ingresa el RUC del cliente (11 digitos)");
            }
        }

        Orden orden = new Orden();
        orden.setEmpresa(empresa);
        orden.setUser(managedUser);
        orden.setTipoComprobante(request.getTipoComprobante());
        orden.setClienteNombre(request.getClienteNombre());
        orden.setClienteApellido(request.getClienteApellido());
        orden.setClienteDni(request.getClienteDni());

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
        // EFECTIVO queda PENDIENTE hasta confirmar entrega del dinero;
        // otros métodos (Yape, tarjeta) también PENDIENTE hasta webhook/confirmación externa
        pago.setEstado(EstadoPago.PENDIENTE);
        Pago pagoGuardado = pagoRepository.save(pago);
        guardada.setPago(pagoGuardado);

        // vaciar carrito
        carrito.getItems().clear();

        // Emitir el comprobante electronico (boleta/factura) automaticamente.
        // FacturaService asigna serie/correlativo y vincula la orden (facturaId).
        facturaService.crear(managedUser, buildFacturaRequest(guardada, request));

        OrdenResponse response = new OrdenResponse(guardada);
        response.setPago(new com.facturacion.pago.PagoResponse(pagoGuardado));
        return response;
    }

    // Convierte una venta del POS en el request del comprobante electronico
    private FacturaRequest buildFacturaRequest(Orden orden, CheckoutRequest request) {
        boolean esFactura = request.getTipoComprobante() == TipoComprobante.FACTURA;

        FacturaRequest fr = new FacturaRequest();
        fr.setTipoComprobante(request.getTipoComprobante());
        fr.setSerie(esFactura ? "F001" : "B001");
        fr.setTipoOperacion("0101");
        fr.setMoneda("PEN");
        fr.setFechaEmision(LocalDate.now().toString());
        fr.setMetodoPago(request.getMetodoPago());
        fr.setOrdenId(orden.getId());

        String nombre = ((request.getClienteNombre() != null ? request.getClienteNombre() : "") + " "
                + (request.getClienteApellido() != null ? request.getClienteApellido() : "")).trim();

        if (esFactura) {
            fr.setTipoDocIdentidad("6");
            fr.setNroDocIdentidad(request.getClienteRuc());
            fr.setClienteNombre(nombre.isEmpty() ? "CLIENTE" : nombre);
        } else if (request.getClienteDni() != null && request.getClienteDni().matches("\\d{8}")) {
            fr.setTipoDocIdentidad("1");
            fr.setNroDocIdentidad(request.getClienteDni());
            fr.setClienteNombre(nombre.isEmpty() ? "CLIENTE" : nombre);
        } else {
            // Boleta sin identificar: permitido por SUNAT para montos menores
            fr.setTipoDocIdentidad("0");
            fr.setNroDocIdentidad("00000000");
            fr.setClienteNombre("CLIENTE VARIOS");
        }

        List<FacturaRequest.ItemRequest> items = new ArrayList<>();
        for (OrdenItem oi : orden.getItems()) {
            FacturaRequest.ItemRequest ir = new FacturaRequest.ItemRequest();
            ir.setDescripcion(oi.getProducto() != null ? oi.getProducto().getNombre() : "Producto");
            ir.setCodProducto(oi.getProducto() != null ? oi.getProducto().getSku() : null);
            ir.setUnidad("NIU");
            ir.setCantidad(oi.getCantidad());
            ir.setMtoValorUnitario(oi.getPrecioUnitario());
            ir.setTipAfeIgv(10); // gravado 18%, igual que el calculo de la orden
            items.add(ir);
        }
        fr.setItems(items);

        return fr;
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
