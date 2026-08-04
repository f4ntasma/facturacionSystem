package com.facturacion.auth.dto;

public class AuthResponse {
    private String token;
    private String nombre;

    public AuthResponse(String token, String nombre) {
        this.token = token;
        this.nombre = nombre;
    }

    public String getToken() {
        return token;
    }

    public String getNombre() {
        return nombre;
    }
}
