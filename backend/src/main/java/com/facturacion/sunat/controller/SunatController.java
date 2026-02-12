package com.facturacion.sunat.controller;

import com.facturacion.sunat.dto.DocumentoSunatRequest;
import com.facturacion.sunat.dto.DocumentoSunatResponse;
import com.facturacion.sunat.exception.SunatException;
import com.facturacion.sunat.service.FirmadorService;
import com.facturacion.sunat.service.SunatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sunat")
@RequiredArgsConstructor
@Tag(name = "SUNAT", description = "Endpoints para facturación electrónica SUNAT")
public class SunatController {
    
    private final SunatService sunatService;
    private final FirmadorService firmadorService;
    
    /**
     * Emite una factura electrónica
     */
    @PostMapping("/factura")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Emitir factura electrónica", 
               description = "Genera y envía una factura electrónica a SUNAT")
    public ResponseEntity<DocumentoSunatResponse> emitirFactura(
            @Valid @RequestBody DocumentoSunatRequest request) throws SunatException {
        
        log.info("Solicitud de emisión de factura: {}-{}", 
            request.getSerie(), request.getNumero());
        
        DocumentoSunatResponse response = sunatService.emitirFactura(request);
        
        return ResponseEntity
            .status(response.isExitoso() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    /**
     * Emite una boleta de venta electrónica
     */
    @PostMapping("/boleta")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Emitir boleta de venta", 
               description = "Genera y envía una boleta de venta electrónica a SUNAT")
    public ResponseEntity<DocumentoSunatResponse> emitirBoleta(
            @Valid @RequestBody DocumentoSunatRequest request) throws SunatException {
        
        log.info("Solicitud de emisión de boleta: {}-{}", 
            request.getSerie(), request.getNumero());
        
        DocumentoSunatResponse response = sunatService.emitirBoleta(request);
        
        return ResponseEntity
            .status(response.isExitoso() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    /**
     * Consulta el estado de un documento en SUNAT
     */
    @GetMapping("/consultar/{tipoDoc}/{serie}/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar estado de documento", 
               description = "Consulta el estado de un documento en SUNAT")
    public ResponseEntity<DocumentoSunatResponse> consultarEstado(
            @PathVariable String tipoDoc,
            @PathVariable String serie,
            @PathVariable Integer numero) {
        
        log.info("Consultando estado: {}-{}-{}", tipoDoc, serie, numero);
        
        DocumentoSunatResponse response = sunatService.consultarEstado(tipoDoc, serie, numero);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verifica el estado del certificado digital
     */
    @GetMapping("/certificado/info")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Información del certificado", 
               description = "Obtiene información del certificado digital")
    public ResponseEntity<Map<String, Object>> infoCertificado() {
        
        Map<String, Object> info = new HashMap<>();
        info.put("vigente", firmadorService.certificadoVigente());
        info.put("detalles", firmadorService.getInfoCertificado());
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Endpoint de prueba para verificar la configuración
     */
    @GetMapping("/test")
    @Operation(summary = "Test de configuración", 
               description = "Verifica que el servicio de SUNAT esté configurado correctamente")
    public ResponseEntity<Map<String, Object>> test() {
        
        Map<String, Object> status = new HashMap<>();
        status.put("servicio", "SUNAT Facturación Electrónica");
        status.put("estado", "Activo");
        status.put("certificado_vigente", firmadorService.certificadoVigente());
        status.put("version", "1.0.0");
        
        return ResponseEntity.ok(status);
    }
}
