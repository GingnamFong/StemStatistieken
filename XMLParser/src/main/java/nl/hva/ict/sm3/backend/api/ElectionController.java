package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import nl.hva.ict.sm3.backend.service.MunicipalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Demo controller for showing how you could load the election data in the backend.
 */
@RestController
@RequestMapping("/elections")
public class ElectionController {
    private final DutchElectionService electionService;

    public ElectionController(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping("/{electionId}")
    public ResponseEntity<Election> getElection(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Automatically load the election if it doesn't exist in cache
            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok(election);
    }

    @GetMapping("/{electionId}/municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Automatically load the election if it doesn't exist in cache
            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }

        List<Municipality> municipalities = election.getAllMunicipalities();
        return ResponseEntity.ok(municipalities);
    }

    @GetMapping("/{electionId}/constituencies")
    public ResponseEntity<List<Constituency>> getConstituencies(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        Election election = electionService.getElectionById(electionId);
        if (election == null) {

            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok(election.getConstituencies());
    }

    @GetMapping("/{electionId}/municipalities/{municipalityId}")
    public ResponseEntity<Municipality> getMunicipalityById(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {
        validateId(electionId, "Election ID");
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Automatically load the election if it doesn't exist in cache
            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(municipality);
    }

    // Optional: endpoint for top parties nationally
    @GetMapping("/{electionId}/top-parties")
    public ResponseEntity<List<Party>> getTopParties(@PathVariable String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Automatically load the election if it doesn't exist in cache
            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.ok(election.getTopParties(3));
    }


    /**
     * Processes the result for a specific election.
     *
     * @param electionId the id of the election, e.g. the value of the Id attribute from the ElectionIdentifier tag.
     * @param folderName the name of the folder that contains the XML result files. If none is provided the value from
     *                   the electionId is used.
     * @return Election if the results have been processed successfully. Please be sure yoy don't output all the data!
     * Just the general data about the election should be sent back to the front-end!<br/>
     * <i>If you want to return something else please feel free to do so!</i>
     */
    @PostMapping("/{electionId}")
    public ResponseEntity<Election> loadElection(@PathVariable String electionId,
                                                 @RequestParam(required = false) String folderName) {
        Election election = electionService.readResults(electionId, folderName != null ? folderName : electionId);
        if (election == null) return ResponseEntity.status(500).build();
        return ResponseEntity.ok(election);
    }

    @PostMapping("/{electionId}/candidatelists")
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
        electionService.loadCandidateLists(election, folder);

        // Get the election from cache after loading 
        Election cachedElection = electionService.getElectionById(electionId);
        return ResponseEntity.ok(cachedElection != null ? cachedElection : election);
    }

    @GetMapping("/{electionId}/candidates/{candidateId}")
    public ResponseEntity<Candidate> getCandidateById(
            @PathVariable String electionId,
            @PathVariable String candidateId) {

        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            // Try to load candidate lists if election not in cache
            election = new Election(electionId);
            electionService.loadCandidateLists(election, electionId);
            // Election is now cached by loadCandidateLists method
            // If loading failed, election will still exist but may be empty
        }

        Candidate candidate = election.getCandidateById(candidateId);
        if (candidate == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(candidate);
    }

    @GetMapping("/{electionId}/pollingstations/postcode/{postalCode}")
    public ResponseEntity<?> getPollingStationByPostalCode(
            @PathVariable String electionId,
            @PathVariable String postalCode) {

        validateId(electionId, "Election ID");
        validatePostalCode(postalCode);

        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.status(404).body("Election not found");
        }

        MunicipalityService muniService = new MunicipalityService(election);
        PollingStation station = muniService.findPollingStationByPostalCode(postalCode);

        if (station == null) {
            return ResponseEntity.status(404)
                    .body("No polling station found for postcode " + postalCode);
        }

        return ResponseEntity.ok(station);
    }

    @GetMapping("/{electionId}/pollingstations")
    public ResponseEntity<List<PollingStation>> getAllPollingStations(
            @PathVariable String electionId) {
        validateId(electionId, "Election ID");

        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.status(404).body(null);
        }

        List<PollingStation> allSb = election.getConstituencies().stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .flatMap(m -> m.getPollingStations().stream())
                .toList();

        return ResponseEntity.ok(allSb);
    }

    private void validateId(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
        String trimmed = value.trim();

        // Letters, cijfers, underscores en dashes toegestaan
        if (!trimmed.matches("^[A-Za-z0-9_-]+$")) {
            throw new IllegalArgumentException(fieldName + " contains illegal characters");
        }
    }

    private void validatePostalCode(String postalCode) {
        if (postalCode == null || postalCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Postal code cannot be empty");
        }

        // Nederlands format tolerant: letters/cijfers/spaties
        if (!postalCode.matches("^[A-Za-z0-9 ]+$")) {
            throw new IllegalArgumentException("Invalid postal code");
        }
    }
}



