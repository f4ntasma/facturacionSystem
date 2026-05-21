package com.facturacion.auth;

public class LoginRequest {
    private String email;
    private String password;

    // Constructores
    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.email = username;
        this.password = password;
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}