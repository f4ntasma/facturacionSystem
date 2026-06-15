package com.facturacion.factura;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class FacturaService {

    private final FacturaRepository facturaRepository;

    public FacturaService(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listar(User user) {
        return facturaRepository.findByEmpresaOrderByCreatedAtDesc(requireEmpresa(user))
                .stream().map(FacturaResponse::new).toList();
    }

    @Transactional
    public FacturaResponse crear(User user, FacturaRequest request) {
        Empresa empresa = requireEmpresa(user);
        Factura f = new Factura();
        f.setEmpresa(empresa);
        f.setUser(user);
        f.setClienteNombre(request.getClienteNombre());
        f.setClienteApellido(request.getClienteApellido());
        f.setClienteDni(request.getClienteDni());
        f.setClienteRuc(request.getClienteRuc());
        f.setTipoComprobante(request.getTipoComprobante());
        f.setMetodoPago(request.getMetodoPago());

        BigDecimal subtotal = BigDecimal.ZERO;
        for (FacturaRequest.ItemRequest ir : request.getItems()) {
            FacturaItem item = new FacturaItem();
            item.setFactura(f);
            item.setProductoNombre(ir.getProductoNombre());
            item.setCantidad(ir.getCantidad());
            item.setPrecioUnitario(ir.getPrecioUnitario());
            BigDecimal itemSubtotal = ir.getPrecioUnitario()
                    .multiply(BigDecimal.valueOf(ir.getCantidad()));
            item.setSubtotal(itemSubtotal);
            f.getItems().add(item);
            subtotal = subtotal.add(itemSubtotal);
        }

        BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(igv).setScale(2, RoundingMode.HALF_UP);
        f.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        f.setIgv(igv);
        f.setTotal(total);

        return new FacturaResponse(facturaRepository.save(f));
    }

    @Transactional
    public FacturaResponse anular(User user, UUID id) {
        Factura f = facturaRepository.findById(id)
                .filter(x -> x.getEmpresa().getId().equals(requireEmpresa(user).getId()))
                .orElseThrow(() -> new NotFoundException("Factura no encontrada"));
        f.setEstado(EstadoFactura.ANULADA);
        return new FacturaResponse(facturaRepository.save(f));
    }

    private Empresa requireEmpresa(User user) {
        if (user.getEmpresa() == null)
            throw new BadRequestException("El usuario no tiene empresa asociada");
        return user.getEmpresa();
    }
}