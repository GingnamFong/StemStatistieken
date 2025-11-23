// java
package hva.nlelections.backend_springboot.controller;

import hva.nlelections.backend_springboot.dto.RegisterRequest;
import hva.nlelections.backend_springboot.model.User;
import hva.nlelections.backend_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User saved = userService.register(req);
        return ResponseEntity.status(201).body("User registered with id: " + saved.getId());
    }
}
