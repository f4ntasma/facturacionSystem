package pe.altoque.sunat;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

/** Lee la constancia de recepción (ApplicationResponse) que devuelve SUNAT. */
final class CdrLector {

    record Cdr(String codigo, String descripcion, List<String> notas) {
        boolean aceptado() {
            int n = numero();
            return n == 0 || n >= 4000;
        }

        int numero() {
            try {
                return Integer.parseInt(codigo.trim());
            } catch (Exception e) {
                return -1;
            }
        }
    }

    private CdrLector() {}

    static Cdr leer(byte[] cdrXml) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(cdrXml));
            String codigo = texto(doc, "ResponseCode");
            String descripcion = texto(doc, "Description");
            List<String> notas = new ArrayList<>();
            NodeList ns = doc.getElementsByTagNameNS("*", "Note");
            for (int i = 0; i < ns.getLength(); i++) notas.add(ns.item(i).getTextContent().trim());
            return new Cdr(codigo == null ? "-1" : codigo, descripcion, notas);
        } catch (Exception e) {
            throw new IllegalStateException("CDR ilegible: " + e.getMessage(), e);
        }
    }

    static String texto(Document doc, String local) {
        NodeList n = doc.getElementsByTagNameNS("*", local);
        return n.getLength() == 0 ? null : n.item(0).getTextContent().trim();
    }
}
