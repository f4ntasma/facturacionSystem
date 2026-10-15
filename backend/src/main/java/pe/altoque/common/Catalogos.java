package pe.altoque.common;

import java.util.LinkedHashMap;
import java.util.Map;

/** Catálogos SUNAT que usa la app (subconjunto útil para negocios pequeños y medianos). */
public final class Catalogos {

    private Catalogos() {}

    /** Catálogo 06: documentos de identidad. */
    public static final Map<String, String> DOCUMENTOS = ordenado(
            "0", "Sin documento",
            "1", "DNI",
            "4", "Carné de extranjería",
            "6", "RUC",
            "7", "Pasaporte",
            "A", "Cédula diplomática");

    /** Catálogo 09: motivos de nota de crédito. */
    public static final Map<String, String> MOTIVOS_NC = ordenado(
            "01", "Anulación de la operación",
            "02", "Anulación por error en el RUC",
            "03", "Corrección por error en la descripción",
            "04", "Descuento global",
            "05", "Descuento por ítem",
            "06", "Devolución total",
            "07", "Devolución por ítem",
            "09", "Disminución en el valor");

    /** Catálogo 10: motivos de nota de débito. */
    public static final Map<String, String> MOTIVOS_ND = ordenado(
            "01", "Intereses por mora",
            "02", "Aumento en el valor",
            "03", "Penalidades / otros conceptos");

    /** Catálogo 03: unidades de medida más usadas. */
    public static final Map<String, String> UNIDADES = ordenado(
            "NIU", "Unidad",
            "ZZ", "Servicio",
            "KGM", "Kilogramo",
            "GRM", "Gramo",
            "LTR", "Litro",
            "MTR", "Metro",
            "BX", "Caja",
            "PK", "Paquete",
            "DZN", "Docena",
            "BG", "Bolsa",
            "BO", "Botella",
            "HUR", "Hora");

    /** Catálogo 07 (resumido). */
    public static final Map<String, String> AFECTACIONES = ordenado(
            "10", "Gravado (con IGV)",
            "20", "Exonerado",
            "30", "Inafecto");

    /** Motivos que devuelven todo el comprobante (se copian todos los ítems y se repone stock). */
    public static boolean motivoTotal(String codigo) {
        return "01".equals(codigo) || "02".equals(codigo) || "06".equals(codigo);
    }

    public static boolean rucValido(String ruc) {
        if (ruc == null || !ruc.matches("(10|15|16|17|20)\\d{9}")) return false;
        int[] pesos = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < 10; i++) suma += (ruc.charAt(i) - '0') * pesos[i];
        int resto = 11 - (suma % 11);
        int digito = resto == 10 ? 0 : resto == 11 ? 1 : resto;
        return digito == ruc.charAt(10) - '0';
    }

    public static boolean documentoValido(String tipo, String numero) {
        if (numero == null) return false;
        return switch (tipo) {
            case "1" -> numero.matches("\\d{8}");
            case "6" -> rucValido(numero);
            case "4", "7", "A" -> numero.matches("[A-Za-z0-9]{4,15}");
            case "0" -> !numero.isBlank();
            default -> false;
        };
    }

    private static Map<String, String> ordenado(String... kv) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put(kv[i], kv[i + 1]);
        return java.util.Collections.unmodifiableMap(m);
    }
}
