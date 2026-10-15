package pe.altoque.comprobante;

import org.springframework.stereotype.Component;
import pe.altoque.common.Montos;
import pe.altoque.comprobante.ComprobanteDtos.*;
import pe.altoque.empresa.Empresa;

import java.util.List;

/** Convierte un comprobante en lo que pinta la app, el ticket impreso y el enlace público. */
@Component
public class ComprobanteVistas {

    private final ComprobanteRepository comprobantes;

    public ComprobanteVistas(ComprobanteRepository comprobantes) {
        this.comprobantes = comprobantes;
    }

    public Vista de(Comprobante c) {
        Empresa e = c.getEmpresa();
        var emisor = new EmisorVista(e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccionFiscal(), e.getDistrito(),
                e.getProvincia(), e.getDepartamento(), e.getTelefono(), e.getEmail(),
                e.getMensajeTicket() != null ? e.getMensajeTicket() : "¡Gracias, caserito! Vuelve al toque.",
                c.getEstablecimiento().getNombre(), e.getAmbienteSunat().name());
        RefVista ref = c.getComprobanteRef() == null ? null
                : new RefVista(c.getComprobanteRef().getId(), c.getRefTipo(), "%s-%06d".formatted(c.getRefSerie(), c.getRefCorrelativo()),
                c.getComprobanteRef().getTokenPublico());
        List<RefVista> notas = c.getTipo().esNota() ? List.of()
                : comprobantes.findByComprobanteRefIdOrderById(c.getId()).stream()
                .map(n -> new RefVista(n.getId(), n.getTipo(), n.numeroImpreso(), n.getTokenPublico())).toList();
        return new Vista(
                c.getId(), c.getTipo(), c.getTipo().nombre(), c.getSerie(), c.getCorrelativo(), c.numeroImpreso(),
                c.getFechaEmision(), c.getHoraEmision(), c.getFechaVencimiento(), c.getMoneda(), c.getFormaPago(),
                c.getClienteTipoDoc(), c.getClienteNumDoc(), c.getClienteNombre(), c.getClienteDireccion(), c.getClienteEmail(),
                c.getClienteTelefono(), c.getIgvPorcentaje(), c.getTotalGravado(), c.getTotalExonerado(), c.getTotalInafecto(),
                c.getTotalIgv(), c.getTotalIcbper(), c.getTotal(), c.getTotalLetras(), c.getObservaciones(),
                c.getEstadoSunat(), c.getSunatCodigo(), c.getSunatMensaje(), c.getHash(),
                c.getEstadoPago(), c.getMontoPagado(), c.getTotal().subtract(c.getMontoPagado()), c.getAnulacionMotivo(),
                ref, c.getMotivoCodigo(), c.getMotivoDescripcion(), notas,
                c.getTokenPublico(), textoQr(c), c.getUsuario().getNombre(), emisor,
                c.getDetalles().stream().map(d -> new DetalleVista(d.getItem(), d.getCodigo(), d.getDescripcion(), d.getUnidadMedida(),
                        d.getCantidad(), d.getPrecioUnitario(), d.getValorUnitario(), d.getTipoAfectacion(), d.getValorVenta(),
                        d.getIgv(), d.getIcbper(), d.getTotal(), d.getProducto() != null ? d.getProducto().getId() : null)).toList(),
                c.getCuotas().stream().map(q -> new CuotaVista(q.getNumero(), q.getMonto(), q.getFechaVencimiento())).toList(),
                c.getPagos().stream().map(p -> new PagoVista(p.getId(), p.getMetodo(), p.getMonto(), p.getReferencia(), p.getPagadoAt())).toList(),
                c.getCreatedAt());
    }

    /** Formato del QR de la representación impresa (Resolución 097-2012/SUNAT). */
    public static String textoQr(Comprobante c) {
        return String.join("|",
                c.getEmpresa().getRuc(), c.getTipo().codigo(), c.getSerie(), String.valueOf(c.getCorrelativo()),
                Montos.txt(c.getTotalIgv()), Montos.txt(c.getTotal()), c.getFechaEmision().toString(),
                c.getClienteTipoDoc(), c.getClienteNumDoc(), c.getHash() == null ? "" : c.getHash()) + "|";
    }
}
