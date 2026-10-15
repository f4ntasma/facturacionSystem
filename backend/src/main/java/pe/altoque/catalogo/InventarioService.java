package pe.altoque.catalogo;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.altoque.common.ApiException;
import pe.altoque.comprobante.Comprobante;
import pe.altoque.comprobante.ComprobanteDetalle;

import java.math.BigDecimal;

/**
 * Único lugar que cambia el stock: cada cambio deja su línea en el kardex.
 * Solo afecta a productos con «controla stock».
 */
@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientos;

    public InventarioService(MovimientoInventarioRepository movimientos) {
        this.movimientos = movimientos;
    }

    /** Salida por venta. Falla si no alcanza el stock. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void vender(Comprobante c) {
        mover(c, -1, MovimientoInventario.Tipo.VENTA, true);
    }

    /** Entrada por nota de crédito con devolución. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void devolver(Comprobante nota) {
        mover(nota, +1, MovimientoInventario.Tipo.DEVOLUCION, false);
    }

    /** Devuelve al stock lo que salió con un comprobante anulado o rechazado. Solo una vez por comprobante. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void revertirVenta(Comprobante c) {
        if (c.getTipo().esNota() || c.getId() == null) return;
        if (movimientos.existsByComprobanteIdAndTipo(c.getId(), MovimientoInventario.Tipo.ANULACION)) return;
        if (!movimientos.existsByComprobanteIdAndTipo(c.getId(), MovimientoInventario.Tipo.VENTA)) return;
        mover(c, +1, MovimientoInventario.Tipo.ANULACION, false);
    }

    /** Ajuste manual: deja el stock en el valor contado y registra la diferencia. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void ajustar(Producto p, BigDecimal nuevoStock, Long usuarioId, String nota) {
        BigDecimal diferencia = nuevoStock.subtract(p.getStock());
        if (diferencia.signum() == 0) return;
        registrar(p, MovimientoInventario.Tipo.AJUSTE, diferencia, null, usuarioId, nota);
    }

    /** Ingreso de mercadería (compra, reposición). */
    @Transactional(propagation = Propagation.MANDATORY)
    public void ingresar(Producto p, BigDecimal cantidad, Long usuarioId, String nota) {
        if (cantidad.signum() <= 0) throw ApiException.invalido("La cantidad que ingresa debe ser mayor a cero.");
        registrar(p, MovimientoInventario.Tipo.INGRESO, cantidad, null, usuarioId, nota);
    }

    private void mover(Comprobante c, int signo, MovimientoInventario.Tipo tipo, boolean validar) {
        for (ComprobanteDetalle d : c.getDetalles()) {
            Producto p = d.getProducto();
            if (p == null || !p.isControlaStock()) continue;
            BigDecimal cantidad = d.getCantidad().multiply(BigDecimal.valueOf(signo));
            if (validar && p.getStock().add(cantidad).signum() < 0) {
                throw ApiException.invalido("No alcanza el stock de «%s»: quedan %s."
                        .formatted(p.getNombre(), p.getStock().stripTrailingZeros().toPlainString()));
            }
            registrar(p, tipo, cantidad, c.getId(), c.getUsuario() != null ? c.getUsuario().getId() : null, null);
        }
    }

    private void registrar(Producto p, MovimientoInventario.Tipo tipo, BigDecimal cantidad, Long comprobanteId, Long usuarioId, String nota) {
        p.setStock(p.getStock().add(cantidad));
        MovimientoInventario m = new MovimientoInventario();
        m.setEmpresaId(p.getEmpresa().getId());
        m.setProducto(p);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setStockResultante(p.getStock());
        m.setComprobanteId(comprobanteId);
        m.setUsuarioId(usuarioId);
        m.setNota(nota);
        movimientos.save(m);
    }
}
