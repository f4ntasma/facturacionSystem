package com.facturacion.sunat.service;

import com.facturacion.sunat.config.SunatConfig;
import com.facturacion.sunat.exception.FirmaDigitalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirmadorService {
    
    private final SunatConfig sunatConfig;
    private KeyStore keyStore;
    private PrivateKey privateKey;
    private X509Certificate certificate;
    
    @PostConstruct
    public void init() {
        try {
            Init.init();
            cargarCertificado();
        } catch (Exception e) {
            log.warn("No se pudo cargar el certificado digital. La firma estará deshabilitada: {}", e.getMessage());
            log.info("Para habilitar la firma, coloca tu certificado en: {}", sunatConfig.getCertificadoPath());
        }
    }
    
    /**
     * Carga el certificado digital desde el archivo .pfx
     */
    private void cargarCertificado() throws Exception {
        try {
            keyStore = KeyStore.getInstance("PKCS12");
            
            InputStream certStream;
            String certPath = sunatConfig.getCertificadoPath();
            
            // Intentar cargar desde classpath o ruta externa
            if (certPath.startsWith("classpath:")) {
                String resourcePath = certPath.substring("classpath:".length());
                Resource resource = new ClassPathResource(resourcePath);
                certStream = resource.getInputStream();
            } else {
                certStream = new FileInputStream(certPath);
            }
            
            char[] password = sunatConfig.getCertificadoPassword().toCharArray();
            keyStore.load(certStream, password);
            certStream.close();
            
            // Obtener el alias del certificado
            String alias = keyStore.aliases().nextElement();
            
            // Cargar la clave privada y el certificado
            privateKey = (PrivateKey) keyStore.getKey(alias, password);
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            
            log.info("Certificado digital cargado exitosamente");
            log.info("Emisor: {}", certificate.getIssuerDN());
            log.info("Válido desde: {} hasta: {}", 
                certificate.getNotBefore(), certificate.getNotAfter());
            
        } catch (Exception e) {
            log.error("Error cargando el certificado digital", e);
            throw new FirmaDigitalException("No se pudo cargar el certificado digital: " + e.getMessage());
        }
    }
    
    /**
     * Firma un documento XML según el estándar UBL 2.1
     */
    public byte[] firmarDocumento(byte[] xmlSinFirmar) throws FirmaDigitalException {
        
        // Si no hay certificado cargado, retornar el XML sin firmar
        if (privateKey == null || certificate == null) {
            log.warn("Certificado no disponible. Retornando XML sin firmar.");
            return xmlSinFirmar;
        }
        
        try {
            // Parsear el XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlSinFirmar));
            
            // Crear la firma XML
            XMLSignature signature = new XMLSignature(
                doc, 
                "", 
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256
            );
            
            // Obtener el elemento raíz y agregar la firma
            Element root = doc.getDocumentElement();
            root.appendChild(signature.getElement());
            
            // Configurar las transformaciones
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            
            // Agregar referencia al documento
            signature.addDocument("", transforms, "http://www.w3.org/2001/04/xmlenc#sha256");
            
            // Agregar información de la clave
            signature.addKeyInfo(certificate);
            
            // Firmar el documento
            signature.sign(privateKey);
            
            // Convertir el documento firmado a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
            
            byte[] xmlFirmado = outputStream.toByteArray();
            log.info("Documento XML firmado exitosamente");
            
            return xmlFirmado;
            
        } catch (Exception e) {
            log.error("Error firmando el documento XML", e);
            throw new FirmaDigitalException("Error al firmar el documento: " + e.getMessage());
        }
    }
    
    public boolean certificadoVigente() {
        if (certificate == null) {
            return false;
        }
        try {
            certificate.checkValidity();
            return true;
        } catch (Exception e) {
            log.warn("Certificado no vigente: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene información del certificado
     */
    public String getInfoCertificado() {
        if (certificate == null) {
            return "Certificado no cargado";
        }
        return String.format(
            "Emisor: %s\nSujeto: %s\nVálido desde: %s\nVálido hasta: %s",
            certificate.getIssuerDN(),
            certificate.getSubjectDN(),
            certificate.getNotBefore(),
            certificate.getNotAfter()
        );
    }
}
