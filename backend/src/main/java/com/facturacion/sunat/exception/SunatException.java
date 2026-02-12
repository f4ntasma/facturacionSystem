package com.facturacion.sunat.exception;

public class SunatException extends Exception {
    
    private String codigoError;
    
    public SunatException(String mensaje) {
        super(mensaje);
    }
    
    public SunatException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    public SunatException(String mensaje, String codigoError) {
        super(mensaje);
        this.codigoError = codigoError;
    }
    
    public String getCodigoError() {
        return codigoError;
    }
}
