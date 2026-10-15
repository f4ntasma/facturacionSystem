package pe.altoque.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Catálogos SUNAT. La fuente es la base (tablas sunat_*, medio_pago); se leen una vez y se guardan en memoria
 * porque solo cambian con una migración. Aquí también viven las validaciones de documentos de identidad.
 */
@Component
public class Catalogos {

    public record Motivo(String codigo, String nombre, boolean devuelveTodo, boolean reponeStock) {}

    public record BienDetraccion(String codigo, String nombre, BigDecimal porcentaje) {}

    public record MedioPago(String codigo, String nombre, String codigoSunat, boolean efectivo) {}

    private final JdbcTemplate jdbc;
    private volatile Datos datos;

    private record Datos(Map<String, String> documentos, Map<String, String> unidades, Map<String, String> afectaciones,
                         Map<String, Motivo> motivosNc, Map<String, Motivo> motivosNd, Map<String, BienDetraccion> detracciones,
                         Map<String, MedioPago> medios) {}

    public Catalogos(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, String> documentos() {
        return datos().documentos();
    }

    public Map<String, String> unidades() {
        return datos().unidades();
    }

    public Map<String, String> afectaciones() {
        return datos().afectaciones();
    }

    /** Motivos de nota de crédito (catálogo 09) o débito (catálogo 10). */
    public Optional<Motivo> motivo(String tipoNota, String codigo) {
        Map<String, Motivo> m = "07".equals(tipoNota) ? datos().motivosNc() : datos().motivosNd();
        return Optional.ofNullable(m.get(codigo));
    }

    public Optional<BienDetraccion> detraccion(String codigo) {
        return Optional.ofNullable(datos().detracciones().get(codigo));
    }

    /** Lo que necesitan los formularios de la app. */
    public Map<String, Object> paraFormularios() {
        Datos d = datos();
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("documentos", d.documentos());
        r.put("motivosNotaCredito", nombres(d.motivosNc()));
        r.put("motivosNotaDebito", nombres(d.motivosNd()));
        r.put("unidades", d.unidades());
        r.put("afectaciones", d.afectaciones());
        r.put("detracciones", d.detracciones().values());
        r.put("mediosPago", d.medios().values());
        return r;
    }

    private Datos datos() {
        Datos d = datos;
        if (d == null) {
            synchronized (this) {
                if (datos == null) datos = cargar();
                d = datos;
            }
        }
        return d;
    }

    private Datos cargar() {
        Map<String, String> documentos = mapa("select codigo, abreviatura from sunat_tipo_documento order by codigo");
        documentos.replace("0", "Sin documento");
        Map<String, String> unidades = mapa("select codigo, nombre from sunat_unidad_medida order by case codigo when 'NIU' then 0 when 'ZZ' then 1 else 2 end, nombre");
        Map<String, String> afectaciones = mapa("select codigo, case codigo when '10' then 'Gravado (con IGV)' when '20' then 'Exonerado' else 'Inafecto' end from sunat_afectacion_igv order by codigo");
        Map<String, Motivo> nc = new LinkedHashMap<>(), nd = new LinkedHashMap<>();
        jdbc.query("select tipo_comprobante, codigo, nombre, devuelve_todo, repone_stock from sunat_motivo_nota order by tipo_comprobante, codigo", rs -> {
            Motivo m = new Motivo(rs.getString(2), rs.getString(3), rs.getBoolean(4), rs.getBoolean(5));
            ("07".equals(rs.getString(1)) ? nc : nd).put(m.codigo(), m);
        });
        Map<String, BienDetraccion> det = new LinkedHashMap<>();
        jdbc.query("select codigo, nombre, porcentaje from sunat_bien_detraccion order by codigo",
                rs -> { det.put(rs.getString(1), new BienDetraccion(rs.getString(1), rs.getString(2), rs.getBigDecimal(3))); });
        Map<String, MedioPago> medios = new LinkedHashMap<>();
        jdbc.query("select codigo, nombre, codigo_sunat, es_efectivo from medio_pago order by orden",
                rs -> { medios.put(rs.getString(1), new MedioPago(rs.getString(1), rs.getString(2), rs.getString(3), rs.getBoolean(4))); });
        return new Datos(documentos, unidades, afectaciones, nc, nd, det, medios);
    }

    private Map<String, String> mapa(String sql) {
        Map<String, String> m = new LinkedHashMap<>();
        jdbc.query(sql, rs -> { m.put(rs.getString(1), rs.getString(2)); });
        return m;
    }

    private static Map<String, String> nombres(Map<String, Motivo> motivos) {
        Map<String, String> m = new LinkedHashMap<>();
        motivos.values().forEach(x -> m.put(x.codigo(), x.nombre()));
        return m;
    }

    // ------------------------------------------------------------------ documentos de identidad

    /** RUC: prefijo válido (10 persona natural, 15/16/17 otros, 20 jurídica) y dígito verificador módulo 11. */
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
}
