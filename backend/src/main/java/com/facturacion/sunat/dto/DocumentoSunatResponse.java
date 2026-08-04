package com.facturacion.sunat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoSunatResponse {
    
    private boolean exitoso;
    private String mensaje;
    private String codigoRespuesta;
    private String descripcionRespuesta;
    
    // Datos del documento
    private String tipoDocumento;
    private String serie;
    private Integer numero;
    private String nombreArchivo;
    
    // Hash y firma
    private String hashCpe;
    private String codigoBarras;
    
    // CDR (Constancia de Recepción)
    private String cdrBase64;
    private String cdrNombreArchivo;
    
    // XML
    private String xmlBase64;
    private String xmlNombreArchivo;
    
    // Errores
    private String codigoError;
    private String mensajeError;
}