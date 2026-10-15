package pe.altoque.comprobante;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

/** Catálogo 01 de SUNAT. Se guarda y viaja como su código ("01", "03", ...). */
public enum TipoComprobante {
    FACTURA("01", "Factura electrónica", 'F'),
    BOLETA("03", "Boleta de venta electrónica", 'B'),
    NOTA_CREDITO("07", "Nota de crédito electrónica", null),
    NOTA_DEBITO("08", "Nota de débito electrónica", null);

    private final String codigo;
    private final String nombre;
    private final Character letraSerie;

    TipoComprobante(String codigo, String nombre, Character letraSerie) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.letraSerie = letraSerie;
    }

    @JsonValue
    public String codigo() {
        return codigo;
    }

    public String nombre() {
        return nombre;
    }

    public boolean esNota() {
        return this == NOTA_CREDITO || this == NOTA_DEBITO;
    }

    /** Valida el formato de serie: F001, B001; notas FC01/BC01 (crédito), FD01/BD01 (débito). */
    public boolean serieValida(String serie) {
        if (serie == null || !serie.matches("[A-Z0-9]{4}")) return false;
        return switch (this) {
            case FACTURA, BOLETA -> serie.charAt(0) == letraSerie;
            case NOTA_CREDITO, NOTA_DEBITO -> serie.charAt(0) == 'F' || serie.charAt(0) == 'B';
        };
    }

    @JsonCreator
    public static TipoComprobante deCodigo(String codigo) {
        return Arrays.stream(values())
                .filter(t -> t.codigo.equals(codigo) || t.name().equalsIgnoreCase(codigo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de comprobante inválido: " + codigo));
    }

    @Converter(autoApply = true)
    public static class JpaConverter implements AttributeConverter<TipoComprobante, String> {
        @Override
        public String convertToDatabaseColumn(TipoComprobante t) {
            return t == null ? null : t.codigo;
        }

        @Override
        public TipoComprobante convertToEntityAttribute(String s) {
            return s == null ? null : deCodigo(s.trim());
        }
    }
}
