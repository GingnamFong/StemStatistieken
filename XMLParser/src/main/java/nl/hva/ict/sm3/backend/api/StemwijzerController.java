package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import nl.hva.ict.sm3.backend.service.StemwijzerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for stemwijzer functionality.
 */
@RestController
@RequestMapping("/api/stemwijzer")
public class StemwijzerController {

    private static final Logger logger = LoggerFactory.getLogger(StemwijzerController.class);
    
    private final StemwijzerService stemwijzerService;
    private final UserRepository userRepository;

    public StemwijzerController(StemwijzerService stemwijzerService, UserRepository userRepository) {
        this.stemwijzerService = stemwijzerService;
        this.userRepository = userRepository;
    }

    /**
     * Calculates match scores between user answers and party positions.
     *
     * @param answers map of questionId to answer
     * @return list of match results
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateMatches(
            @RequestBody Map<Integer, String> answers) {
        
        logger.info("POST /api/stemwijzer/calculate - Calculating matches for {} answers", answers != null ? answers.size() : 0);
        
        if (answers == null || answers.isEmpty()) {
            logger.warn("Invalid request: answers are null or empty");
            return ResponseEntity.badRequest().body(Map.of("error", "Answers cannot be empty"));
        }

        try {
            List<Map<String, Object>> results = stemwijzerService.calculateMatches(answers);
            logger.debug("Successfully calculated {} matches", results.size());
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            logger.error("Election data not available: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "TK2025 election data not available",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Failed to calculate matches: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to calculate matches",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * Sets the favorite party for a user based on stemwijzer results.
     *
     * @param userId user ID
     * @param request contains partyId
     * @return success or error response
     */
    @PostMapping("/favorite-party/{userId}")
    public ResponseEntity<?> setFavoriteParty(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {
        
        logger.info("POST /api/stemwijzer/favorite-party/{} - Setting favorite party", userId);
        
        if (request == null || !request.containsKey("partyId")) {
            logger.warn("Invalid request: partyId is missing for user {}", userId);
            return ResponseEntity.badRequest().body("partyId is required");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            logger.warn("User not found: {}", userId);
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        String partyId = request.get("partyId").toString();
        user.setFavoriteParty(partyId);
        userRepository.save(user);

        logger.debug("Favorite party updated for user {}: {}", userId, partyId);
        return ResponseEntity.ok(Map.of("message", "Favorite party updated", "favoriteParty", user.getFavoriteParty()));
    }
}
