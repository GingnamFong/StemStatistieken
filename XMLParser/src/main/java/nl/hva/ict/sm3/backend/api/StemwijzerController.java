package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.service.StemwijzerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for stemwijzer functionality.
 */
@RestController
@RequestMapping("/api/stemwijzer")
public class StemwijzerController {

    private final StemwijzerService stemwijzerService;

    public StemwijzerController(StemwijzerService stemwijzerService) {
        this.stemwijzerService = stemwijzerService;
    }

    /**
     * Calculates match scores between user answers and party positions.
     *
     * @param answers map of questionId to answer
     * @return list of match results
     */
    @PostMapping("/calculate")
    public ResponseEntity<List<Map<String, Object>>> calculateMatches(
            @RequestBody Map<Integer, String> answers) {
        
        if (answers == null || answers.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<Map<String, Object>> results = stemwijzerService.calculateMatches(answers);
        return ResponseEntity.ok(results);
    }
}
