package com.facturacion.sunat.exception;

public class FirmaDigitalException extends RuntimeException {
    
    public FirmaDigitalException(String mensaje) {
        super(mensaje);
    }
    
    public FirmaDigitalException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
