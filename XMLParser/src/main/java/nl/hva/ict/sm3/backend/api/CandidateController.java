package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.service.CandidateListService;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling candidate-related endpoints.
 */
@RestController
@RequestMapping("/elections/{electionId}/candidates")
public class CandidateController {
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
     * @return Election with candidate lists loaded
     */
    @PostMapping("/lists")
    public ResponseEntity<Election> loadCandidateLists(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {

        String folder = folderName != null ? folderName : electionId;

        // Check if election already exists in cache 
        Election election = electionService.getElectionById(electionId);
        if (election != null && !election.getCandidates().isEmpty()) {
            return ResponseEntity.ok(election);
        }

        if (election == null) {
            election = new Election(electionId);
        }

        // Load candidate lists into the election (preserves existing data if election was already cached)
        candidateListService.loadCandidateLists(election, folder);

        // Get the election from cache after loading 
        Election cachedElection = electionService.getElectionById(electionId);
        return ResponseEntity.ok(cachedElection != null ? cachedElection : election);
    }

    /**
     * Retrieves a specific candidate by ID for a given election.
     *
     * @param electionId the id of the election
     * @param candidateId the id of the candidate
     * @return Candidate if found, 404 if not found
     */
    @GetMapping("/{candidateId}")
    public ResponseEntity<Candidate> getCandidateById(
            @PathVariable String electionId,
            @PathVariable String candidateId) {

        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Try to load candidate lists if election not in cache
            election = new Election(electionId);
            candidateListService.loadCandidateLists(election, electionId);
            // Election is now cached by loadCandidateLists method
            // If loading failed, election will still exist but may be empty
        }

        Candidate candidate = election.getCandidateById(candidateId);
        if (candidate == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(candidate);
    }
}

