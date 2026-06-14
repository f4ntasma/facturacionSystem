package com.facturacion.mesa;

import java.util.UUID;

public class MesaResponse {
    private UUID id;
    private String nombre;
    private Integer piso;
    private String estado;

    public MesaResponse(Mesa m) {
        this.id = m.getId();
        this.nombre = m.getNombre();
        this.piso = m.getPiso();
        this.estado = m.getEstado().name();
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public Integer getPiso() { return piso; }
    public String getEstado() { return estado; }
}