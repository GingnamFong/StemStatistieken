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
import java.time.LocalDate;
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
        response.put("birthDate", user.getBirthDate());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("email", user.getEmail());
            response.put("birthDate", user.getBirthDate());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get user");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> updates) {
        LocalDate birthDate = null;
        if (updates.containsKey("birthDate") && updates.get("birthDate") != null) {
            String birthDateStr = updates.get("birthDate").toString();
            if (!birthDateStr.isEmpty() && !birthDateStr.equals("null")) {
                birthDate = LocalDate.parse(birthDateStr);
            }
        }
        
        String firstName = null;
        if (updates.containsKey("firstName") && updates.get("firstName") != null) {
            String firstNameStr = updates.get("firstName").toString().trim();
            if (!firstNameStr.isEmpty() && !firstNameStr.equals("null")) {
                firstName = firstNameStr;
            }
        }
        
        String lastName = null;
        if (updates.containsKey("lastName") && updates.get("lastName") != null) {
            String lastNameStr = updates.get("lastName").toString().trim();
            if (!lastNameStr.isEmpty() && !lastNameStr.equals("null")) {
                lastName = lastNameStr;
            }
        }
        
        User updated = userService.updateUser(userId, firstName, lastName, birthDate);
        Map<String, Object> response = new HashMap<>();
        response.put("id", updated.getId());
        response.put("firstName", updated.getFirstName());
        response.put("lastName", updated.getLastName());
        response.put("email", updated.getEmail());
        response.put("birthDate", updated.getBirthDate());
        return ResponseEntity.ok(response);
    }
}
