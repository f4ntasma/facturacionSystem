package com.facturacion.empresa;

import java.util.UUID;

public class EmpresaResponse {
    private UUID id;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String telefono;

    public EmpresaResponse(Empresa empresa) {
        this.id = empresa.getId();
        this.ruc = empresa.getRuc();
        this.razonSocial = empresa.getRazonSocial();
        this.direccion = empresa.getDireccion();
        this.telefono = empresa.getTelefono();
    }

    public UUID getId() {
        return id;
    }

    public String getRuc() {
        return ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }
}
