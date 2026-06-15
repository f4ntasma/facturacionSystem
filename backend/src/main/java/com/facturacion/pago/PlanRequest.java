package com.facturacion.pago;

public class PlanRequest {
    private String planNombre;
    private double precio;

    public String getPlanNombre() { return planNombre; }
    public void setPlanNombre(String planNombre) { this.planNombre = planNombre; }
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
}