package com.facturacion.factura;

import com.facturacion.common.BadRequestException;
import com.facturacion.common.NotFoundException;
import com.facturacion.empresa.Empresa;
import com.facturacion.orden.Orden;
import com.facturacion.orden.OrdenRepository;
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
    private final OrdenRepository ordenRepository;

    public FacturaService(FacturaRepository facturaRepository, OrdenRepository ordenRepository) {
        this.facturaRepository = facturaRepository;
        this.ordenRepository = ordenRepository;
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

        // Documento
        f.setSerie(request.getSerie());
        // El correlativo lo asigna el backend (ultimo + 1 por serie) para
        // garantizar consecutividad sin saltos, requisito de SUNAT.
        f.setCorrelativo(siguienteCorrelativo(empresa, request.getSerie()));
        f.setTipoComprobante(request.getTipoComprobante());
        f.setTipoOperacion(request.getTipoOperacion() != null ? request.getTipoOperacion() : "0101");
        f.setMoneda(request.getMoneda() != null ? request.getMoneda() : "PEN");
        f.setFechaEmision(request.getFechaEmision());
        f.setFechaVencimiento(request.getFechaVencimiento());

        // Cliente
        f.setTipoDocIdentidad(request.getTipoDocIdentidad());
        f.setClienteNombre(request.getClienteNombre());
        f.setClienteDireccion(request.getClienteDireccion());
        // Mapear nroDocIdentidad al campo correcto
        String tipoDoc = request.getTipoDocIdentidad();
        if ("6".equals(tipoDoc)) {
            f.setClienteRuc(request.getNroDocIdentidad());
        } else {
            f.setClienteDni(request.getNroDocIdentidad());
        }

        // Pago y adicional
        f.setMetodoPago(request.getMetodoPago());
        f.setObservacion(request.getObservacion());
        f.setLeyenda(request.getLeyenda());
        f.setNroOrdenCompra(request.getNroOrdenCompra());
        f.setGuiaRemision(request.getGuiaRemision());

        // Items con calculo de IGV por afectacion
        BigDecimal gravadas = BigDecimal.ZERO;
        BigDecimal exoneradas = BigDecimal.ZERO;
        BigDecimal inafectas = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;

        for (FacturaRequest.ItemRequest ir : request.getItems()) {
            FacturaItem item = new FacturaItem();
            item.setFactura(f);
            item.setCodProducto(ir.getCodProducto());
            item.setUnidad(ir.getUnidad() != null ? ir.getUnidad() : "NIU");
            item.setDescripcion(ir.getDescripcion());
            item.setProductoNombre(ir.getDescripcion()); // backward compat
            item.setCantidad(ir.getCantidad());
            item.setPrecioUnitario(ir.getMtoValorUnitario());

            int tipAfe = ir.getTipAfeIgv() != null ? ir.getTipAfeIgv() : 10;
            item.setTipAfeIgv(tipAfe);

            BigDecimal mtoValorVenta = ir.getMtoValorUnitario()
                    .multiply(BigDecimal.valueOf(ir.getCantidad()))
                    .setScale(2, RoundingMode.HALF_UP);
            item.setMtoValorVenta(mtoValorVenta);
            item.setSubtotal(mtoValorVenta); // backward compat

            BigDecimal itemIgv = BigDecimal.ZERO;
            // Catalogo 07 SUNAT:
            // 10-16 = Gravado (IGV 18%)
            // 20    = Exonerado
            // 30-36 = Inafecto
            // 40    = Exportacion (tasa 0%)
            if (tipAfe >= 10 && tipAfe <= 16) {
                itemIgv = mtoValorVenta.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
                gravadas = gravadas.add(mtoValorVenta);
            } else if (tipAfe == 20) {
                exoneradas = exoneradas.add(mtoValorVenta);
            } else {
                // 30-36 inafecto, 40 exportacion — sin IGV
                inafectas = inafectas.add(mtoValorVenta);
            }
            item.setIgv(itemIgv);
            totalIgv = totalIgv.add(itemIgv);

            f.getItems().add(item);
        }

        BigDecimal subtotal = gravadas.add(exoneradas).add(inafectas);
        BigDecimal total = subtotal.add(totalIgv).setScale(2, RoundingMode.HALF_UP);

        f.setMtoOperGravadas(gravadas.setScale(2, RoundingMode.HALF_UP));
        f.setMtoOperExoneradas(exoneradas.setScale(2, RoundingMode.HALF_UP));
        f.setMtoOperInafectas(inafectas.setScale(2, RoundingMode.HALF_UP));
        f.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        f.setIgv(totalIgv.setScale(2, RoundingMode.HALF_UP));
        f.setTotal(total);

        Factura guardada = facturaRepository.save(f);

        // Vincular la venta (orden) con su comprobante si el request lo indica
        if (request.getOrdenId() != null) {
            Orden orden = ordenRepository.findById(request.getOrdenId())
                    .filter(o -> o.getUser().getId().equals(user.getId()))
                    .orElseThrow(() -> new NotFoundException("Venta no encontrada"));
            if (orden.getFacturaId() != null) {
                throw new BadRequestException("Esta venta ya tiene un comprobante emitido");
            }
            orden.setFacturaId(guardada.getId());
            ordenRepository.save(orden);
        }

        return new FacturaResponse(guardada);
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

    private String siguienteCorrelativo(Empresa empresa, String serie) {
        String max = facturaRepository.findMaxCorrelativo(empresa.getId(), serie).orElse(null);
        long siguiente = 1;
        if (max != null && !max.isBlank()) {
            try {
                siguiente = Long.parseLong(max) + 1;
            } catch (NumberFormatException e) {
                siguiente = 1;
            }
        }
        return String.format("%08d", siguiente);
    }
}
