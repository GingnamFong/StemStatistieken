package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.PollingStation;
import nl.hva.ict.sm3.backend.service.PollingStationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for polling station related endpoints.
 *
 * <p>This controller exposes read-only operations for polling stations
 * within a specific election.</p>
 */
@RestController
@RequestMapping("/elections/{electionId}/pollingstations")
public class PollingStationController {

    private final PollingStationService pollingStationService;

    /**
     * Constructs the controller with an injected {@link PollingStationService}.
     *
     * @param pollingStationService service handling polling station logic
     */
    public PollingStationController(PollingStationService pollingStationService) {
        this.pollingStationService = pollingStationService;
    }

    /**
     * Returns all polling stations for a given election.
     *
     * @param electionId ID of the election
     * @return list of polling stations or 404 if election not found
     */
    @GetMapping
    public ResponseEntity<List<PollingStation>> getAllPollingStations(
            @PathVariable String electionId) {

        List<PollingStation> stations =
                pollingStationService.getAllPollingStations(electionId);

        if (stations.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(stations);
    }

    /**
     * Finds a polling station by postal code within an election.
     *
     * @param electionId ID of the election
     * @param postalCode postal code to search for
     * @return polling station or 404 if not found
     */
    @GetMapping("/postcode/{postalCode}")
    public ResponseEntity<PollingStation> getPollingStationByPostalCode(
            @PathVariable String electionId,
            @PathVariable String postalCode) {

        PollingStation station =
                pollingStationService.findByPostalCode(electionId, postalCode);

        if (station == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(station);
    }
}
