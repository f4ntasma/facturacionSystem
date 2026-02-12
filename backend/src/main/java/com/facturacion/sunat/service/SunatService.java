package com.facturacion.sunat.service;

import com.facturacion.sunat.config.SunatConfig;
import com.facturacion.sunat.dto.DocumentoSunatRequest;
import com.facturacion.sunat.dto.DocumentoSunatResponse;
import com.facturacion.sunat.exception.SunatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SunatService {
    
    private final SunatConfig sunatConfig;
    private final FirmadorService firmadorService;
    
    /**
     * Emite una factura electrónica
     */
    public DocumentoSunatResponse emitirFactura(DocumentoSunatRequest request) throws SunatException {
        try {
            log.info("Iniciando emisión de factura: {}-{}", request.getSerie(), request.getNumero());
            
            // 1. Construir el XML UBL
            String xmlContent = construirXmlFactura(request);
            byte[] xmlBytes = xmlContent.getBytes(StandardCharsets.UTF_8);
            
            // 2. Firmar el XML
            byte[] xmlFirmado = firmadorService.firmarDocumento(xmlBytes);
            
            // 3. Crear el archivo ZIP
            String nombreArchivo = String.format("%s-%s-%s-%s",
                sunatConfig.getRuc(),
                request.getTipoDocumento(),
                request.getSerie(),
                request.getNumero()
            );
            byte[] zipBytes = crearZip(nombreArchivo + ".xml", xmlFirmado);
            
            // 4. Por ahora retornamos respuesta simulada (implementar envío real después)
            return procesarRespuestaSimulada(request, xmlFirmado, nombreArchivo);
            
        } catch (Exception e) {
            log.error("Error emitiendo factura", e);
            throw new SunatException("Error al emitir factura: " + e.getMessage(), e);
        }
    }
    
    /**
     * Emite una boleta de venta electrónica
     */
    public DocumentoSunatResponse emitirBoleta(DocumentoSunatRequest request) throws SunatException {
        request.setTipoDocumento("03");
        return emitirFactura(request);
    }
    
    /**
     * Construye el XML UBL 2.1 manualmente
     */
    private String construirXmlFactura(DocumentoSunatRequest request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String fechaEmision = request.getFechaEmision().format(formatter);
        
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\" ");
        xml.append("xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\" ");
        xml.append("xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\" ");
        xml.append("xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\">\n");
        
        // UBL Version
        xml.append("  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>\n");
        xml.append("  <cbc:CustomizationID>2.0</cbc:CustomizationID>\n");
        
        // Identificación del documento
        xml.append("  <cbc:ID>").append(request.getSerie()).append("-").append(request.getNumero()).append("</cbc:ID>\n");
        xml.append("  <cbc:IssueDate>").append(fechaEmision).append("</cbc:IssueDate>\n");
        xml.append("  <cbc:InvoiceTypeCode listID=\"0101\">").append(request.getTipoDocumento()).append("</cbc:InvoiceTypeCode>\n");
        xml.append("  <cbc:DocumentCurrencyCode>").append(request.getTipoMoneda()).append("</cbc:DocumentCurrencyCode>\n");
        
        // Proveedor (Emisor)
        xml.append("  <cac:AccountingSupplierParty>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyIdentification>\n");
        xml.append("        <cbc:ID schemeID=\"6\">").append(sunatConfig.getRuc()).append("</cbc:ID>\n");
        xml.append("      </cac:PartyIdentification>\n");
        xml.append("      <cac:PartyLegalEntity>\n");
        xml.append("        <cbc:RegistrationName><![CDATA[").append(sunatConfig.getRazonSocial()).append("]]></cbc:RegistrationName>\n");
        xml.append("      </cac:PartyLegalEntity>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingSupplierParty>\n");
        
        // Cliente
        DocumentoSunatRequest.ClienteDto cliente = request.getCliente();
        xml.append("  <cac:AccountingCustomerParty>\n");
        xml.append("    <cac:Party>\n");
        xml.append("      <cac:PartyIdentification>\n");
        xml.append("        <cbc:ID schemeID=\"").append(cliente.getTipoDocumento()).append("\">").append(cliente.getNumeroDocumento()).append("</cbc:ID>\n");
        xml.append("      </cac:PartyIdentification>\n");
        xml.append("      <cac:PartyLegalEntity>\n");
        xml.append("        <cbc:RegistrationName><![CDATA[").append(cliente.getRazonSocial()).append("]]></cbc:RegistrationName>\n");
        xml.append("      </cac:PartyLegalEntity>\n");
        xml.append("    </cac:Party>\n");
        xml.append("  </cac:AccountingCustomerParty>\n");
        
        // Totales
        xml.append("  <cac:LegalMonetaryTotal>\n");
        xml.append("    <cbc:PayableAmount currencyID=\"").append(request.getTipoMoneda()).append("\">")
           .append(request.getImporteTotal()).append("</cbc:PayableAmount>\n");
        xml.append("  </cac:LegalMonetaryTotal>\n");
        
        // Items
        for (int i = 0; i < request.getItems().size(); i++) {
            DocumentoSunatRequest.ItemDto item = request.getItems().get(i);
            xml.append("  <cac:InvoiceLine>\n");
            xml.append("    <cbc:ID>").append(i + 1).append("</cbc:ID>\n");
            xml.append("    <cbc:InvoicedQuantity unitCode=\"").append(item.getUnidadMedida()).append("\">")
               .append(item.getCantidad()).append("</cbc:InvoicedQuantity>\n");
            xml.append("    <cbc:LineExtensionAmount currencyID=\"").append(request.getTipoMoneda()).append("\">")
               .append(item.getValorVenta()).append("</cbc:LineExtensionAmount>\n");
            xml.append("    <cac:Item>\n");
            xml.append("      <cbc:Description><![CDATA[").append(item.getDescripcion()).append("]]></cbc:Description>\n");
            xml.append("    </cac:Item>\n");
            xml.append("    <cac:Price>\n");
            xml.append("      <cbc:PriceAmount currencyID=\"").append(request.getTipoMoneda()).append("\">")
               .append(item.getPrecioUnitario()).append("</cbc:PriceAmount>\n");
            xml.append("    </cac:Price>\n");
            xml.append("  </cac:InvoiceLine>\n");
        }
        
        xml.append("</Invoice>");
        
        return xml.toString();
    }
    
    /**
     * Crea un archivo ZIP con el XML firmado
     */
    private byte[] crearZip(String nombreArchivo, byte[] xmlFirmado) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(nombreArchivo);
            zos.putNextEntry(entry);
            zos.write(xmlFirmado);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
    
    /**
     * Procesa respuesta simulada (implementar envío real después)
     */
    private DocumentoSunatResponse procesarRespuestaSimulada(
            DocumentoSunatRequest request,
            byte[] xmlFirmado,
            String nombreArchivo) {
        
        log.info("Generando respuesta simulada para: {}", nombreArchivo);
        
        return DocumentoSunatResponse.builder()
            .exitoso(true)
            .mensaje("Documento generado exitosamente (envío a SUNAT pendiente de configuración)")
            .codigoRespuesta("0")
            .descripcionRespuesta("XML generado y firmado correctamente")
            .tipoDocumento(request.getTipoDocumento())
            .serie(request.getSerie())
            .numero(request.getNumero())
            .nombreArchivo(nombreArchivo)
            .xmlBase64(Base64.getEncoder().encodeToString(xmlFirmado))
            .xmlNombreArchivo(nombreArchivo + ".xml")
            .build();
    }
    
    /**
     * Consulta el estado de un documento en SUNAT
     */
    public DocumentoSunatResponse consultarEstado(String tipoDoc, String serie, Integer numero) {
        log.info("Consultando estado: {}-{}-{}", tipoDoc, serie, numero);
        
        return DocumentoSunatResponse.builder()
            .tipoDocumento(tipoDoc)
            .serie(serie)
            .numero(numero)
            .mensaje("Funcionalidad de consulta en desarrollo")
            .build();
    }
}
