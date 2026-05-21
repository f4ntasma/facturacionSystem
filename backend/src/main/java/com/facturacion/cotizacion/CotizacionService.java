package com.facturacion.cotizacion;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;

    public CotizacionService(CotizacionRepository cotizacionRepository) {
        this.cotizacionRepository = cotizacionRepository;
    }

    @Transactional (readOnly = true)
    public List<CotizacionResponse> listar(User user) {
        Empresa empresa = requireEmpresa(user);
        return cotizacionRepository.findByEmpresaOrderByCreatedAtDesc(empresa)
                .stream().map(CotizacionResponse::new).toList();
    }

    @Transactional
    public CotizacionResponse crear(User user, CotizacionRequest request) {
        Empresa empresa = requireEmpresa(user);
        Cotizacion c = new Cotizacion();
        c.setEmpresa(empresa);
        c.setCliente(request.getCliente());

        for (CotizacionRequest.ItemRequest ir : request.getItems()) {
            CotizacionItem item = new CotizacionItem();
            item.setCotizacion(c);
            item.setNombre(ir.getNombre());
            item.setCantidad(ir.getCantidad());
            item.setPrecio(ir.getPrecio());
            c.getItems().add(item);
        }

        BigDecimal total = c.getItems().stream()
                .map(i -> i.getPrecio().multiply(BigDecimal.valueOf(i.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        c.setTotal(total);

        return new CotizacionResponse(cotizacionRepository.save(c));
    }

    @Transactional
    public void eliminar(User user, UUID id) {
        Empresa empresa = requireEmpresa(user);
        Cotizacion c = cotizacionRepository.findById(id)
                .filter(x -> x.getEmpresa().getId().equals(empresa.getId()))
                .orElseThrow(() -> new NotFoundException("Cotización no encontrada"));
        cotizacionRepository.delete(c);
    }

    private Empresa requireEmpresa(User user) {
        if (user.getEmpresa() == null) {
            throw new BadRequestException("El usuario no tiene empresa asociada");
        }
        return user.getEmpresa();
    }
}