// java
package hva.nlelections.backend_springboot.controller;

import hva.nlelections.backend_springboot.dto.LoginRequest;
import hva.nlelections.backend_springboot.dto.RegisterRequest;
import hva.nlelections.backend_springboot.model.User;
import hva.nlelections.backend_springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.login(req.getEmail(), req.getPassword());
        Map<String, Object> response = new HashMap<>();
        response.put("token", "dummy-token-" + user.getId());
        response.put("userId", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        return ResponseEntity.ok(response);
    }
}
