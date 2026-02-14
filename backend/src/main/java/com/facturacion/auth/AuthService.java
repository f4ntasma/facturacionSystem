package com.facturacion.auth;

import com.facturacion.auth.jwt.JwtService;
import com.facturacion.user.User;
import com.facturacion.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import com.facturacion.auth.LoginResponse.UserInfo;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager, 
                      JwtService jwtService,
                      UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) throws Exception {
        try {
            String emailOrUsername = loginRequest.getUsername();
            User user = userRepository.findByEmail(emailOrUsername)
                    .orElseThrow(() -> new Exception("Usuario no encontrado"));
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), loginRequest.getPassword())
            );
            String token = jwtService.generateToken(user);
            UserInfo userInfo = new UserInfo(
                user.getId().getMostSignificantBits(),
                user.getNombre(),
                user.getEmail()
            );
            return new LoginResponse(token, userInfo);
        } catch (AuthenticationException e) {
            throw new Exception("Credenciales inválidas");
        }
    }
}