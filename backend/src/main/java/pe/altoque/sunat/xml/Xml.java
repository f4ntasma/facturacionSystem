package pe.altoque.sunat.xml;

/** Escritor mínimo de XML (sin sangrías: lo que se firma es exactamente lo que se envía). */
public final class Xml {

    private final StringBuilder sb = new StringBuilder(4096);

    public Xml() {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    public Xml abrir(String tag, String... atributos) {
        sb.append('<').append(tag);
        for (int i = 0; i + 1 < atributos.length; i += 2) {
            sb.append(' ').append(atributos[i]).append("=\"").append(escapar(atributos[i + 1])).append('"');
        }
        sb.append('>');
        return this;
    }

    public Xml cerrar(String tag) {
        sb.append("</").append(tag).append('>');
        return this;
    }

    /** Elemento con texto; si el valor es null o vacío no se escribe. */
    public Xml el(String tag, Object valor, String... atributos) {
        if (valor == null || valor.toString().isEmpty()) return this;
        abrir(tag, atributos);
        sb.append(escapar(valor.toString()));
        return cerrar(tag);
    }

    public Xml vacio(String tag) {
        sb.append('<').append(tag).append("/>");
        return this;
    }

    public static String escapar(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (char ch : s.toCharArray()) {
            switch (ch) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&apos;");
                default -> {
                    if (ch >= 0x20 || ch == '\n' || ch == '\t' || ch == '\r') out.append(ch);
                }
            }
        }
        return out.toString();
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
