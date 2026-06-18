package com.facturacion.auth;

import com.facturacion.auth.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200") // Permitir requests desde Angular
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authService.authenticate(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("Usuario o contraseña incorrectos");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        MessageResponse messageResponse = new MessageResponse("Logout exitoso");
        return ResponseEntity.ok(messageResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new ErrorResponse(e.getMessage()));
        }
    }
}