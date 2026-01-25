package com.facturacion.auth;

public class LoginResponse {
    private String token;
    private UserInfo user;

    // Constructores
    public LoginResponse() {}

    public LoginResponse(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    // Clase interna para información del usuario
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;

        public UserInfo() {}

        public UserInfo(Long id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
        }

        // Getters y Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}