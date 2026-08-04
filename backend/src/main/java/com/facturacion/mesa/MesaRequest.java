package com.facturacion.mesa;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MesaRequest {
    @NotBlank
    private String nombre;

    @NotNull
    private Integer piso;

    private EstadoMesa estado;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getPiso() { return piso; }
    public void setPiso(Integer piso) { this.piso = piso; }
    public EstadoMesa getEstado() { return estado; }
    public void setEstado(EstadoMesa estado) { this.estado = estado; }
}