package com.facturacion.auth;

import org.springframework.stereotype.Service;
import com.facturacion.auth.LoginResponse.UserInfo;

@Service
public class AuthService {

    public LoginResponse authenticate(LoginRequest loginRequest) throws Exception {
        // Por ahora, validación simple para testing
        // En producción, aquí validarías contra la base de datos
        if ("admin".equals(loginRequest.getUsername()) && "admin".equals(loginRequest.getPassword())) {
            // Generar token (por ahora uno simple, en producción usarías JWT)
            String token = "simple-token-" + System.currentTimeMillis();
            
            UserInfo user = new UserInfo(1L, "admin", "admin@facturacion.com");
            
            return new LoginResponse(token, user);
        } else {
            throw new Exception("Credenciales inválidas");
        }
    }
}