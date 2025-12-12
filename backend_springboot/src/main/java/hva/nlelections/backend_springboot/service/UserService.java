// java
package hva.nlelections.backend_springboot.service;

import hva.nlelections.backend_springboot.dto.RegisterRequest;
import hva.nlelections.backend_springboot.model.User;
import hva.nlelections.backend_springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Password rule: min 8, at least one uppercase, one lowercase, one digit (special optional)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$");

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        String raw = req.getPassword();
        if (!PASSWORD_PATTERN.matcher(raw).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 chars, include upper, lower and digit");
        }

        User u = new User();
        u.setFirstName(req.getName().trim());
        u.setLastName(req.getLastName() != null ? req.getLastName().trim() : null);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(raw));

        return userRepository.save(u);
    }

    @Transactional(readOnly = true)
    public User login(String emailRaw, String passwordRaw) {
        String email = emailRaw.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(passwordRaw, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional
    public User updateUser(Long id, String firstName, String lastName, java.time.LocalDate birthDate, String favoriteParty) {
        User user = getUserById(id);
        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null) {
            user.setLastName(lastName.trim().isEmpty() ? null : lastName.trim());
        }
        if (birthDate != null) {
            user.setBirthDate(birthDate);
        }
        if (favoriteParty != null) {
            user.setFavoriteParty(favoriteParty.trim().isEmpty() ? null : favoriteParty.trim());
        }
        return userRepository.save(user);
    }
}
