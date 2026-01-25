package com.facturacion;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Backend funcionando correctamente vvita, esto se va a prender!";
    }
}