package com.facturacion.cliente;

import java.util.UUID;

public class ClienteResponse {
    private UUID id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String direccion;
    private String email;

    public ClienteResponse(Cliente c) {
        this.id = c.getId();
        this.nombre = c.getNombre();
        this.apellido = c.getApellido();
        this.dni = c.getDni();
        this.telefono = c.getTelefono();
        this.direccion = c.getDireccion();
        this.email = c.getEmail();
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getEmail() { return email; }
}