package com.facturacion.sunat.exception;

import com.facturacion.common.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class SunatExceptionHandler {
    
    /**
     * Maneja errores de conexión con SUNAT
     */
    @ExceptionHandler(SunatException.class)
    public ResponseEntity<ApiError> handleSunatException(
            SunatException ex, 
            WebRequest request) {
        
        log.error("Error de SUNAT: {}", ex.getMessage(), ex);
        
        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_GATEWAY.value())
            .error("Error de Comunicación con SUNAT")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        if (ex.getCodigoError() != null) {
            error.setMessage(String.format("Código SUNAT: %s - %s", 
                ex.getCodigoError(), ex.getMessage()));
        }
        
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(error);
    }
    
    /**
     * Maneja errores de firma digital
     */
    @ExceptionHandler(FirmaDigitalException.class)
    public ResponseEntity<ApiError> handleFirmaDigitalException(
            FirmaDigitalException ex, 
            WebRequest request) {
        
        log.error("Error de firma digital: {}", ex.getMessage(), ex);
        
        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Error de Firma Digital")
            .message("No se pudo firmar el documento: " + ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
    
    /**
     * Maneja errores de validación de certificado
     */
    @ExceptionHandler(java.security.cert.CertificateException.class)
    public ResponseEntity<ApiError> handleCertificateException(
            java.security.cert.CertificateException ex, 
            WebRequest request) {
        
        log.error("Error de certificado: {}", ex.getMessage(), ex);
        
        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Error de Certificado Digital")
            .message("El certificado digital no es válido o ha expirado")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }
    
    /**
     * Maneja errores de timeout con SUNAT
     */
    @ExceptionHandler({
        java.net.SocketTimeoutException.class,
        java.net.ConnectException.class
    })
    public ResponseEntity<ApiError> handleTimeoutException(
            Exception ex, 
            WebRequest request) {
        
        log.error("Error de conexión: {}", ex.getMessage(), ex);
        
        ApiError error = ApiError.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.GATEWAY_TIMEOUT.value())
            .error("Timeout de Conexión")
            .message("No se pudo conectar con los servicios de SUNAT. Intente nuevamente.")
            .path(request.getDescription(false).replace("uri=", ""))
            .build();
        
        return ResponseEntity
            .status(HttpStatus.GATEWAY_TIMEOUT)
            .body(error);
    }
}
