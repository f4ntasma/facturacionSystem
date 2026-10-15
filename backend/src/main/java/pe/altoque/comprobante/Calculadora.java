package pe.altoque.comprobante;

import pe.altoque.common.Montos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Cálculos de línea y totales. El precio ingresado incluye IGV (así vende la bodega); de ahí sale el valor unitario.
 * El IGV de la línea se obtiene como total − valor venta, para que la suma cuadre siempre al céntimo.
 */
public final class Calculadora {

    /** Impuesto a las bolsas plásticas por unidad (vigente desde 2023). */
    public static final BigDecimal ICBPER_UNITARIO = new BigDecimal("0.50");

    public static final String GRAVADO = "10";
    public static final String EXONERADO = "20";
    public static final String INAFECTO = "30";

    private Calculadora() {}

    public static void calcularLinea(ComprobanteDetalle d, BigDecimal igvPorcentaje, boolean aplicaIcbper) {
        BigDecimal cantidad = d.getCantidad();
        BigDecimal precio = d.getPrecioUnitario();
        BigDecimal bruto = Montos.r2(cantidad.multiply(precio));
        if (GRAVADO.equals(d.getTipoAfectacion())) {
            BigDecimal factor = BigDecimal.ONE.add(igvPorcentaje.divide(Montos.CIEN, 6, RoundingMode.HALF_UP));
            d.setValorUnitario(precio.divide(factor, 10, RoundingMode.HALF_UP));
            BigDecimal valorVenta = Montos.r2(cantidad.multiply(precio).divide(factor, MathContext.DECIMAL64));
            d.setValorVenta(valorVenta);
            d.setIgv(bruto.subtract(valorVenta));
        } else {
            d.setValorUnitario(precio.setScale(10, RoundingMode.HALF_UP));
            d.setValorVenta(bruto);
            d.setIgv(BigDecimal.ZERO.setScale(2));
        }
        BigDecimal icbper = aplicaIcbper ? Montos.r2(cantidad.setScale(0, RoundingMode.DOWN).multiply(ICBPER_UNITARIO)) : BigDecimal.ZERO;
        d.setIcbper(icbper);
        d.setTotal(bruto.add(icbper));
    }

    public static void totalizar(Comprobante c) {
        BigDecimal gravado = BigDecimal.ZERO, exonerado = BigDecimal.ZERO, inafecto = BigDecimal.ZERO;
        BigDecimal igv = BigDecimal.ZERO, icbper = BigDecimal.ZERO;
        for (ComprobanteDetalle d : c.getDetalles()) {
            switch (d.getTipoAfectacion()) {
                case GRAVADO -> gravado = gravado.add(d.getValorVenta());
                case EXONERADO -> exonerado = exonerado.add(d.getValorVenta());
                default -> inafecto = inafecto.add(d.getValorVenta());
            }
            igv = igv.add(d.getIgv());
            icbper = icbper.add(d.getIcbper());
        }
        c.setTotalGravado(Montos.r2(gravado));
        c.setTotalExonerado(Montos.r2(exonerado));
        c.setTotalInafecto(Montos.r2(inafecto));
        c.setTotalIgv(Montos.r2(igv));
        c.setTotalIcbper(Montos.r2(icbper));
        c.setTotal(Montos.r2(gravado.add(exonerado).add(inafecto).add(igv).add(icbper)));
    }

    /** Suma sin IGV (LineExtensionAmount del comprobante). */
    public static BigDecimal valorVentaTotal(Comprobante c) {
        return Montos.r2(c.getTotalGravado().add(c.getTotalExonerado()).add(c.getTotalInafecto()));
    }
}
