package pe.altoque.sunat.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

/** Firma XMLDSig enveloped dentro de ext:ExtensionContent, como lo exige SUNAT. */
public final class FirmaXml {

    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    public record Resultado(byte[] xml, String hash) {}

    private FirmaXml() {}

    public static Resultado firmar(String xml, PrivateKey clave, X509Certificate certificado) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            NodeList contenidos = doc.getElementsByTagNameNS(NS_EXT, "ExtensionContent");
            if (contenidos.getLength() == 0) throw new IllegalStateException("El XML no tiene ext:ExtensionContent");
            Node destino = contenidos.item(contenidos.getLength() - 1);

            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            Reference ref = fac.newReference("",
                    fac.newDigestMethod(DigestMethod.SHA1, null),
                    List.of(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);
            SignedInfo si = fac.newSignedInfo(
                    fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    List.of(ref));
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            X509Data datos = kif.newX509Data(List.of(certificado.getSubjectX500Principal().getName(), certificado));
            KeyInfo ki = kif.newKeyInfo(List.of(datos));

            DOMSignContext ctx = new DOMSignContext(clave, destino);
            ctx.setDefaultNamespacePrefix("ds");
            XMLSignature firma = fac.newXMLSignature(si, ki, null, UblBuilder.FIRMA_ID, null);
            firma.sign(ctx);

            String hash = java.util.Base64.getEncoder().encodeToString(ref.getDigestValue());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            var t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.transform(new DOMSource(doc), new StreamResult(out));
            return new Resultado(out.toByteArray(), hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo firmar el XML: " + e.getMessage(), e);
        }
    }
}
