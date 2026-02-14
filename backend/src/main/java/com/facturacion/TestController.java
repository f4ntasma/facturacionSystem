package com.facturacion;

import com.facturacion.user.User;
import com.facturacion.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test")
    public String test() {
        return "Backend funcionando correctamente vvita, esto se va a prender!";
    }
    
    @GetMapping("/test/hash/{password}")
    public Map<String, String> generateHash(@PathVariable("password") String password) {
        Map<String, String> response = new HashMap<>();
        String hash = passwordEncoder.encode(password);
        response.put("password", password);
        response.put("hash", hash);
        return response;
    }
    
    @GetMapping("/test/user/{email}")
    public Map<String, Object> testUser(@PathVariable("email") String email, @RequestParam(value = "password", required = false) String password) {
        Map<String, Object> response = new HashMap<>();
        
        var userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            response.put("found", false);
            response.put("message", "Usuario no encontrado");
            return response;
        }
        
        User user = userOpt.get();
        response.put("found", true);
        response.put("email", user.getEmail());
        response.put("nombre", user.getNombre());
        response.put("role", user.getRole());
        response.put("activo", user.isActivo());
        response.put("passwordHash", user.getPassword().substring(0, 20) + "...");
        
        if (password != null) {
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            response.put("passwordMatches", matches);
        }
        
        return response;
    }
}