package pe.altoque.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Montos {

    public static final BigDecimal CIEN = new BigDecimal("100");

    private Montos() {}

    public static BigDecimal r2(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    /** Monto con dos decimales para el XML. */
    public static String txt(BigDecimal v) {
        return r2(nz(v)).toPlainString();
    }
}
