package pe.altoque.comprobante;

import org.springframework.stereotype.Component;
import pe.altoque.common.Catalogos;
import pe.altoque.common.Montos;
import pe.altoque.comprobante.ComprobanteDtos.*;
import pe.altoque.empresa.Empresa;
import pe.altoque.sunat.EnvioSunat;
import pe.altoque.sunat.EnvioSunatRepository;
import pe.altoque.sunat.LoteSunatItem;
import pe.altoque.sunat.LoteSunatRepository;

import java.util.List;

/** Convierte un comprobante en lo que pinta la app, el ticket impreso y el enlace público. */
@Component
public class ComprobanteVistas {

    private final ComprobanteRepository comprobantes;
    private final EnvioSunatRepository envios;
    private final LoteSunatRepository lotes;
    private final Catalogos catalogos;

    public ComprobanteVistas(ComprobanteRepository comprobantes, EnvioSunatRepository envios, LoteSunatRepository lotes,
                             Catalogos catalogos) {
        this.comprobantes = comprobantes;
        this.envios = envios;
        this.lotes = lotes;
        this.catalogos = catalogos;
    }

    public Vista de(Comprobante c) {
        Empresa e = c.getEmpresa();
        var emisor = new EmisorVista(e.getRuc(), e.getRazonSocial(), e.getNombreComercial(), e.getDireccionFiscal(), e.getDistrito(),
                e.getProvincia(), e.getDepartamento(), e.getTelefono(), e.getEmail(),
                e.getMensajeTicket() != null ? e.getMensajeTicket() : "¡Gracias, caserito! Vuelve al toque.",
                c.getEstablecimiento().getNombre(), e.getAmbienteSunat().name());
        RefVista ref = c.getNota() == null ? null
                : new RefVista(c.getComprobanteRef() != null ? c.getComprobanteRef().getId() : null, c.getRefTipo(),
                "%s-%06d".formatted(c.getRefSerie(), c.getRefCorrelativo()),
                c.getComprobanteRef() != null ? c.getComprobanteRef().getTokenPublico() : null);
        List<RefVista> notas = c.getTipo().esNota() ? List.of()
                : comprobantes.notasDe(c.getId()).stream()
                .map(n -> new RefVista(n.getId(), n.getTipo(), n.numeroImpreso(), n.getTokenPublico())).toList();
        var ultimo = envios.findFirstByComprobanteIdOrderByIdDesc(c.getId());
        String anulacion = lotes.itemsDe(c.getId()).stream().findFirst().map(LoteSunatItem::getMotivo).orElse(null);
        DetraccionVista detraccion = c.getDetraccion() == null ? null : new DetraccionVista(c.getDetraccion().getBienCodigo(),
                catalogos.detraccion(c.getDetraccion().getBienCodigo()).map(Catalogos.BienDetraccion::nombre).orElse(null),
                c.getDetraccion().getPorcentaje(), c.getDetraccion().getMonto(), c.getDetraccion().getCuentaBanco());
        return new Vista(
                c.getId(), c.getTipo(), c.getTipo().nombre(), c.getSerie(), c.getCorrelativo(), c.numeroImpreso(),
                c.getFechaEmision(), c.getHoraEmision(), c.getFechaVencimiento(), c.getMoneda(), c.getFormaPago(),
                c.getClienteTipoDoc(), c.getClienteNumDoc(), c.getClienteNombre(), c.getClienteDireccion(), c.getClienteEmail(),
                c.getClienteTelefono(), c.getIgvPorcentaje(), c.getTotalGravado(), c.getTotalExonerado(), c.getTotalInafecto(),
                c.getTotalIgv(), c.getTotalIcbper(), c.getTotal(), c.getTotalLetras(), c.getObservaciones(),
                c.getEstadoSunat(), ultimo.map(EnvioSunat::getCodigo).orElse(null), ultimo.map(EnvioSunat::getMensaje).orElse(null),
                c.getHash(), c.getEstadoPago(), c.getMontoPagado(), c.getSaldo(), c.getNetoACobrar(), detraccion, anulacion,
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
