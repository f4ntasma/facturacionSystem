package com.facturacion.common;

// Enum unico de metodos de pago para todo el sistema.
// Union de los valores usados por ordenes (EFECTIVO, TARJETA, YAPE, PLIN)
// y facturas (CONTADO, CREDITO, TARJETA, YAPE, PLIN) — compatible con
// todos los registros existentes en la BD.
public enum MetodoPago {
    EFECTIVO,
    TARJETA,
    YAPE,
    PLIN,
    CONTADO,
    CREDITO
}
