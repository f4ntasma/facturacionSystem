package com.facturacion.common;

// Enum compartido entre los modulos orden y factura.
// EnumType.STRING guarda solo el nombre (BOLETA/FACTURA), por lo que
// mover el enum de paquete no afecta los datos existentes en la BD.
public enum TipoComprobante {
    BOLETA,
    FACTURA
}
