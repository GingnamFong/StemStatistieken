package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.service.CandidateListService;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling candidate-related endpoints.
 */
@RestController
@RequestMapping("/elections/{electionId}/candidates")
public class CandidateController {
    private static final Logger logger = LoggerFactory.getLogger(CandidateController.class);
    
    private final DutchElectionService electionService;
    private final CandidateListService candidateListService;

    /**
     * Constructs a new CandidateController with the required services.
     *
     * @param electionService the service for managing elections
     * @param candidateListService the service for loading candidate lists
     */
    public CandidateController(DutchElectionService electionService, CandidateListService candidateListService) {
        this.electionService = electionService;
        this.candidateListService = candidateListService;
    }

    /**
     * Loads candidate lists for a specific election.
     *
     * @param electionId the id of the election
     * @param folderName the name of the folder that contains the XML candidate list files. 
     *                   If none is provided, the value from the electionId is used.
     * @return Election with candidate lists loaded, or error response with appropriate status code
     */
    @PostMapping("/lists")
    public ResponseEntity<?> loadCandidateLists(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {
        
        logger.info("POST /elections/{}/candidates/lists - Loading candidate lists", electionId);
        
        try {
            // Validate election ID
            validateElectionId(electionId);
            
            String folder = folderName != null ? folderName : electionId;
            logger.debug("Using folder name: {} for election: {}", folder, electionId);

            // Check if election already exists in cache with candidates
            Election election = electionService.getElectionById(electionId);
            if (election != null && !election.getCandidates().isEmpty()) {
                logger.info("Candidate lists already loaded for election: {} ({} candidates)", 
                        electionId, election.getCandidates().size());
                return ResponseEntity.ok(election);
            }

            if (election == null) {
                logger.debug("Election {} not found in cache, creating new election object", electionId);
                election = new Election(electionId);
            }

            // Load candidate lists into the election
            logger.debug("Loading candidate lists for election: {} from folder: {}", electionId, folder);
            candidateListService.loadCandidateLists(election, folder);

            // Get the election from cache after loading 
            Election cachedElection = electionService.getElectionById(electionId);
            Election finalElection = cachedElection != null ? cachedElection : election;
            
            if (finalElection.getCandidates().isEmpty()) {
                logger.warn("No candidates loaded for election: {}", electionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("No candidates found for election " + electionId + 
                                ". Please ensure the candidate list files exist."));
            }
            
            logger.info("Successfully loaded {} candidates for election: {}", 
                    finalElection.getCandidates().size(), electionId);
            // Return Election object directly for backward compatibility with frontend
            return ResponseEntity.ok(finalElection);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for loading candidate lists - electionId: {}, error: {}", 
                    electionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error loading candidate lists for election: {}", electionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to load candidate lists: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific candidate by ID for a given election.
     *
     * @param electionId the id of the election
     * @param candidateId the id of the candidate
     * @return Candidate if found, 404 with error message if not found, 400 if invalid IDs
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<?> getCandidateById(
            @PathVariable String electionId,
            @PathVariable String candidateId) {
        
        logger.info("GET /elections/{}/candidates/{} - Retrieving candidate", electionId, candidateId);
        
        try {
            // Validate IDs
            validateElectionId(electionId);
            validateCandidateId(candidateId);

            // Only get from database/cache, don't parse XML
            Election election = electionService.getElectionById(electionId);
            if (election == null) {
                logger.warn("Election not found: {}", electionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Election with ID '" + electionId + "' not found"));
            }

            Candidate candidate = election.getCandidateById(candidateId);
            if (candidate == null) {
                logger.warn("Candidate not found: {} in election: {}", candidateId, electionId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Candidate with ID '" + candidateId + 
                                "' not found in election '" + electionId + "'"));
            }

            logger.debug("Successfully retrieved candidate: {} from election: {}", candidateId, electionId);
            return ResponseEntity.ok(candidate);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for candidate - electionId: {}, candidateId: {}, error: {}", 
                    electionId, candidateId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving candidate {} from election {}", candidateId, electionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve candidate: " + e.getMessage()));
        }
    }

    /**
     * Creates a standardized error response.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    /**
     * Validates election ID format.
     */
    private void validateElectionId(String electionId) {
        if (electionId == null || electionId.trim().isEmpty()) {
            throw new IllegalArgumentException("Election ID cannot be null or empty");
        }
        String trimmed = electionId.trim();
        if (!trimmed.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException("Election ID contains invalid characters. Only letters, numbers, underscores, and dashes are allowed");
        }
    }

    /**
     * Validates candidate ID format.
     */
    private void validateCandidateId(String candidateId) {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate ID cannot be null or empty");
        }
        String trimmed = candidateId.trim();
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Candidate ID is too long (max 255 characters)");
        }
    }
}

