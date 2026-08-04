package com.facturacion.sunat.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class DocumentoSunatRequest {
    
    private String tipoDocumento; 
    private String serie;
    private Integer numero;
    private LocalDate fechaEmision;
    private String tipoMoneda = "PEN";
    
    // Cliente
    private ClienteDto cliente;
    
    // Items
    private List<ItemDto> items;
    
    // Totales
    private BigDecimal totalGravadas;
    private BigDecimal totalExoneradas;
    private BigDecimal totalInafectas;
    private BigDecimal totalIgv;
    private BigDecimal totalDescuentos;
    private BigDecimal totalOtrosCargos;
    private BigDecimal importeTotal;
    
    // Observaciones
    private String observaciones;
    
    @Data
    public static class ClienteDto {
        private String tipoDocumento; 
        private String numeroDocumento;
        private String razonSocial;
        private String direccion;
        private String email;
    }
    
    @Data
    public static class ItemDto {
        private Integer cantidad;
        private String unidadMedida = "NIU";
        private String descripcion;
        private BigDecimal valorUnitario;
        private BigDecimal precioUnitario;
        private String codigoProducto;
        private BigDecimal descuento;
        private String tipoIgv; 
        private BigDecimal igv;
        private BigDecimal totalImpuestos;
        private BigDecimal valorVenta;
    }
}