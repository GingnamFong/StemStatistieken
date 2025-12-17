package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.repository.ElectionRepository;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import nl.hva.ict.sm3.backend.service.MunicipalityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller responsible for exposing all API endpoints related to
 * Dutch election data. This includes retrieving election results, constituencies,
 * municipalities, candidates, and polling station information.
 *
 * <p>The controller performs lightweight validation (e.g., ID format checks)
 * and delegates all business logic to the {@link DutchElectionService} or
 * {@link MunicipalityService}. This ensures the controller remains thin,
 * testable, and focused purely on HTTP concerns.</p>
 */
@RestController
@RequestMapping("/elections")
public class ElectionController {
    private final DutchElectionService electionService;
    private final ElectionRepository electionRepository;
    
    /**
     * Constructs the controller with an injected {@link DutchElectionService}.
     *
     * @param electionService service that loads, caches, and provides election data
     * @param electionRepository repository for paginated election queries
     */
    public ElectionController(DutchElectionService electionService, ElectionRepository electionRepository) {
        this.electionService = electionService;
        this.electionRepository = electionRepository;
    }

    /**
     * Returns a list of all available elections.
     * @return list of election IDs
     */
    @GetMapping
    public ResponseEntity<List<String>> getAllElections() {
        List<String> electionIds = electionService.getAllElectionIds();
        return ResponseEntity.ok(electionIds);
    }

    /**
     * Returns paginated list of elections.
     * Supports pagination via Pageable (page, size, sort parameters).
     * 
     * @param pageable Pagination parameters (default: page=0, size=10)
     * @return Page of elections
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<Election>> getAllElectionsPaginated(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Election> elections = electionRepository.findAllElections(pageable);
        return ResponseEntity.ok(elections);
    }

    @GetMapping("/{electionId}")
    public ResponseEntity<Election> getElection(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        // Only get from database/cache, don't parse XML
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(election);
    }
    /**
     * Retrieves all municipalities for the given election.
     *
     * @param electionId ID of the election
     * @return list of municipalities or {@code 404 Not Found} if the election could not be loaded
     */
    @GetMapping("/{electionId}/municipalities")
    public ResponseEntity<List<Municipality>> getMunicipalities(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        // Only get from database/cache, don't parse XML
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }

        List<Municipality> municipalities = election.getAllMunicipalities();
        return ResponseEntity.ok(municipalities);
    }
    /**
     * Returns a list of constituencies (kieskringen) for the given election.
     *
     * @param electionId ID of the election
     * @return list of constituencies or {@code 404 Not Found} if unavailable
     */
    @GetMapping("/{electionId}/constituencies")
    public ResponseEntity<List<Constituency>> getConstituencies(@PathVariable String electionId) {
        validateId(electionId, "Election ID");
        // Only get from database/cache, don't parse XML
        Election election = electionService.getElectionById(electionId);
        if (election == null) {

            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                return ResponseEntity.notFound().build();
            }
        }

        return ResponseEntity.ok(election.getConstituencies());
    }
    /**
     * Retrieves a specific municipality by its ID within a given election.
     *
     * @param electionId     the election identifier
     * @param municipalityId ID of the requested municipality
     * @return matching municipality or {@code 404 Not Found}
     */
    @GetMapping("/{electionId}/municipalities/{municipalityId}")
    public ResponseEntity<Municipality> getMunicipalityById(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {
        validateId(electionId, "Election ID");
        // Only get from database/cache, don't parse XML
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(municipality);
    }
    /**
     * Returns the national top-3 political parties based on vote counts.
     *
     * @param electionId ID of the election
     * @return top-3 parties or {@code 404 Not Found} if the election is missing
     */

    // Optional: endpoint for top parties nationally
    @GetMapping("/{electionId}/top-parties")
    public ResponseEntity<List<Party>> getTopParties(@PathVariable String electionId) {
        // Only get from database/cache, don't parse XML
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
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



