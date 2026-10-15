package pe.altoque.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Monto en letras para la leyenda 1000 del comprobante: "DOCE CON 50/100 SOLES". */
public final class NumeroALetras {

    private static final String[] UNIDADES = {"", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE",
            "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE",
            "VEINTE", "VEINTIUNO", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS", "VEINTISIETE",
            "VEINTIOCHO", "VEINTINUEVE"};
    private static final String[] DECENAS = {"", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
    private static final String[] CENTENAS = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

    private NumeroALetras() {}

    public static String convertir(BigDecimal monto, String moneda) {
        BigDecimal m = monto.setScale(2, RoundingMode.HALF_UP);
        long entero = m.longValue();
        int centimos = m.remainder(BigDecimal.ONE).movePointRight(2).abs().intValue();
        String nombreMoneda = "USD".equals(moneda) ? "DÓLARES AMERICANOS" : "SOLES";
        String letras = entero == 0 ? "CERO" : enLetras(entero);
        return "%s CON %02d/100 %s".formatted(letras, centimos, nombreMoneda);
    }

    static String enLetras(long n) {
        if (n == 0) return "";
        if (n < 30) return UNIDADES[(int) n];
        if (n < 100) {
            int d = (int) (n / 10), u = (int) (n % 10);
            return DECENAS[d] + (u > 0 ? " Y " + UNIDADES[u] : "");
        }
        if (n == 100) return "CIEN";
        if (n < 1000) {
            int c = (int) (n / 100);
            return (CENTENAS[c] + " " + enLetras(n % 100)).trim();
        }
        if (n < 1_000_000) {
            long miles = n / 1000;
            String prefijo = miles == 1 ? "MIL" : apocope(enLetras(miles)) + " MIL";
            return (prefijo + " " + enLetras(n % 1000)).trim();
        }
        long millones = n / 1_000_000;
        String prefijo = millones == 1 ? "UN MILLON" : apocope(enLetras(millones)) + " MILLONES";
        return (prefijo + " " + enLetras(n % 1_000_000)).trim();
    }

    /** "VEINTIUNO MIL" se dice "VEINTIUN MIL"; "TREINTA Y UNO MIL", "TREINTA Y UN MIL". */
    private static String apocope(String s) {
        if (s.endsWith("UNO")) return s.substring(0, s.length() - 1);
        return s;
    }
}
