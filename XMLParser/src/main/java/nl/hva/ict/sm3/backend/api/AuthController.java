package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5174", "http://localhost:5173", "https://hva-frontend.onrender.com"})
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // Validate request
        if (request.name == null || request.name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Naam is verplicht");
        }
        if (request.email == null || request.email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is verplicht");
        }
        if (!isValidEmail(request.email)) {
            return ResponseEntity.badRequest().body("Ongeldig emailadres");
        }
        if (request.password == null || request.password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Wachtwoord is verplicht");
        }
        if (!isValidPassword(request.password)) {
            return ResponseEntity.badRequest().body("Wachtwoord moet minimaal 8 tekens bevatten, met minimaal één hoofdletter, één kleine letter en één cijfer");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is al geregistreerd");
        }

        // Create new user
        User user = new User();
        user.setName(request.name.trim());
        user.setLastName(request.lastName != null ? request.lastName.trim() : null);
        user.setEmail(request.email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.password));

        try {
            User savedUser = userRepository.save(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registratie geslaagd");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fout bij registratie: " + e.getMessage());
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    private boolean isValidPassword(String password) {
        // At least 8 characters, one lowercase, one uppercase, one digit
        return password != null && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Validate request
        if (request.email == null || request.email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Email is verplicht");
        }
        if (request.password == null || request.password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Wachtwoord is verplicht");
        }

        // Find user by email
        Optional<User> userOpt = userRepository.findByEmail(request.email.trim().toLowerCase());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ongeldige e-mail of wachtwoord");
        }

        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ongeldige e-mail of wachtwoord");
        }

        // Generate token (simple token for now, in production use JWT)
        String token = "user-" + user.getId() + "-" + System.currentTimeMillis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("userId", user.getId());
        response.put("token", token);
        response.put("email", user.getEmail());
        
        return ResponseEntity.ok(response);
    }

    // Inner class for request DTOs
    public static class RegisterRequest {
        public String name;
        public String lastName;
        public String email;
        public String password;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}

