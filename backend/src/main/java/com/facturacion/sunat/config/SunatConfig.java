package com.facturacion.sunat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sunat")
public class SunatConfig {
    
    // Configuración de credenciales SUNAT
    private String ruc;
    private String usuario;
    private String clave;
    
    // URLs de servicios SUNAT
    private String urlFactura;
    private String urlGuia;
    private String urlRetenciones;
    
    // Configuración de certificado digital
    private String certificadoPath;
    private String certificadoPassword;
    
    // Configuración de ambiente (BETA o PRODUCCION)
    private boolean produccion = false;
    
    // Configuración de empresa
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String ubigeo;
    private String departamento;
    private String provincia;
    private String distrito;
    private String urbanizacion;
    private String codigoPais = "PE";
}
