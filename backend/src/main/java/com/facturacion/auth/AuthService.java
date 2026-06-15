package com.facturacion.auth;

import com.facturacion.auth.dto.RegisterRequest;
import com.facturacion.auth.jwt.JwtService;
import com.facturacion.common.BadRequestException;
import com.facturacion.user.Role;
import com.facturacion.user.User;
import com.facturacion.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.facturacion.auth.LoginResponse.UserInfo;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, 
                      JwtService jwtService,
                      UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse authenticate(LoginRequest loginRequest) throws Exception {
        try {
            String emailOrUsername = loginRequest.getEmail();
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

    public LoginResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("El email ya está registrado");
        }

        User user = new User();
        user.setNombre(request.getNombre());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setActivo(true);
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        UserInfo userInfo = new UserInfo(
                user.getId().getMostSignificantBits(),
                user.getNombre(),
                user.getEmail()
        );
        return new LoginResponse(token, userInfo);
    }


}