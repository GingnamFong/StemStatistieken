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
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5174", "http://localhost:5173", "https://hva-frontend.onrender.com", "http://13.48.214.231", "http://stemstatistieken.me", "https://stemstatistieken.me"})
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
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Je gebruiker bestaat niet");
        }

        User user = userOpt.get();
        
        // Verify password
        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ongeldig wachtwoord");
        }

        // Generate token (simple token for now, in production use JWT)
        String token = "user-" + user.getId() + "-" + System.currentTimeMillis();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("userId", user.getId());
        response.put("token", token);
        response.put("email", user.getEmail());
        // Map 'name' to 'firstName' for frontend compatibility
        response.put("firstName", user.getName());
        response.put("lastName", user.getLastName());
        response.put("birthDate", user.getBirthDate());
        
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

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        // Map 'name' uit de DB naar 'firstName' voor de frontend
        response.put("firstName", user.getName()); 
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("birthDate", user.getBirthDate());
        response.put("favoriteParty", user.getFavoriteParty());
        response.put("profilePicture", user.getProfilePicture());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();

        // Update velden indien aanwezig in request
        if (updates.containsKey("firstName")) {
            String firstName = (String) updates.get("firstName");
            if (firstName != null && !firstName.isBlank()) {
                user.setName(firstName.trim());
            }
        }

        if (updates.containsKey("lastName")) {
            String lastName = (String) updates.get("lastName");
            // LastName mag leeg zijn of null
            user.setLastName(lastName != null ? lastName.trim() : null);
        }

        if (updates.containsKey("birthDate")) {
            Object birthDateObj = updates.get("birthDate");
            if (birthDateObj != null) {
                String birthDateStr = birthDateObj.toString();
                if (!birthDateStr.isBlank()) {
                    try {
                        // Parse as LocalDate first (format: YYYY-MM-DD)
                        LocalDate date = LocalDate.parse(birthDateStr);
                        
                        // Validate: birthDate cannot be in the future
                        LocalDate today = LocalDate.now();
                        if (date.isAfter(today)) {
                            return ResponseEntity.badRequest()
                                    .body("Geboortedatum kan niet in de toekomst liggen");
                        }
                        
                        // Convert to LocalDateTime at start of day
                        user.setBirthDate(date.atStartOfDay());
                    } catch (DateTimeParseException e) {
                        // If parsing fails, try parsing as LocalDateTime directly
                        try {
                            LocalDateTime dateTime = LocalDateTime.parse(birthDateStr);
                            
                            // Validate: birthDate cannot be in the future
                            LocalDateTime now = LocalDateTime.now();
                            if (dateTime.isAfter(now)) {
                                return ResponseEntity.badRequest()
                                        .body("Geboortedatum kan niet in de toekomst liggen");
                            }
                            
                            user.setBirthDate(dateTime);
                        } catch (DateTimeParseException e2) {
                            return ResponseEntity.badRequest()
                                    .body("Ongeldig datumformaat. Gebruik YYYY-MM-DD");
                        }
                    }
                } else {
                    user.setBirthDate(null);
                }
            } else {
                user.setBirthDate(null);
            }
        }

        if (updates.containsKey("favoriteParty")) {
            Object favoritePartyObj = updates.get("favoriteParty");
            if (favoritePartyObj != null) {
                String favoriteParty = favoritePartyObj.toString();
                user.setFavoriteParty(favoriteParty.isBlank() ? null : favoriteParty.trim());
            } else {
                user.setFavoriteParty(null);
            }
        }

        if (updates.containsKey("profilePicture")) {
            Object profilePictureObj = updates.get("profilePicture");
            if (profilePictureObj != null) {
                String profilePicture = profilePictureObj.toString();
                user.setProfilePicture(profilePicture.isBlank() ? null : profilePicture);
            } else {
                user.setProfilePicture(null);
            }
        }

        try {
            userRepository.save(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fout bij opslaan: " + e.getMessage());
        }

        // Stuur bijgewerkte data terug
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("firstName", user.getName());
        response.put("lastName", user.getLastName());
        response.put("email", user.getEmail());
        response.put("birthDate", user.getBirthDate());
        response.put("favoriteParty", user.getFavoriteParty());
        response.put("profilePicture", user.getProfilePicture());

        return ResponseEntity.ok(response);
    }
}

