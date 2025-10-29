package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.Party;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Demo controller for showing how you could load the election data in the backend.
 */
@RestController
@RequestMapping("elections")
public class ElectionController {
    private final DutchElectionService electionService;

    public ElectionController(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    @GetMapping("{electionId}")
    public ResponseEntity<Election> getElection(@PathVariable String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(election);
    }

    // DIT IS ALLEEN OM DE TOP 3 PARTIJEN TE LADEN IN POSTMAN OMDAT HET ZO LANG DUURT OM ALLES TE LADEN
    private Map<String, Object> getMunicipalitySummary(Municipality m) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", m.getId());
        summary.put("name", m.getName());
        summary.put("validVotes", m.getValidVotes());
        summary.put("topParties", m.getTopPartiesWithNames(3)); // top 3 partijen
        return summary;
    }

    @GetMapping("{electionId}/municipalities")
    public ResponseEntity<List<Map<String, Object>>> getMunicipalitiesSummary(@PathVariable String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) return ResponseEntity.notFound().build();

        List<Map<String, Object>> summaries = election.getConstituencies()
                .stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .map(this::getMunicipalitySummary)
                .toList();

        return ResponseEntity.ok(summaries);
    }
    @GetMapping("{electionId}/municipalities/{municipalityId}/parties")
    public ResponseEntity<Map<String, Object>> getAllPartiesForMunicipality(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {

        Election election = electionService.getElectionById(electionId);
        if (election == null) return ResponseEntity.notFound().build();

        Municipality municipality = election.getConstituencies()
                .stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .filter(m -> m.getId().equals(municipalityId))
                .findFirst()
                .orElse(null);

        if (municipality == null) return ResponseEntity.notFound().build();

        Map<String, Object> response = new HashMap<>();
        response.put("id", municipality.getId());
        response.put("name", municipality.getName());
        response.put("validVotes", municipality.getValidVotes());
        response.put("parties", municipality.getAllPartiesWithNames());

        return ResponseEntity.ok(response);
    }


    // Optional: endpoint for top parties nationally
    @GetMapping("{electionId}/top-parties")
    public ResponseEntity<List<Party>> getTopParties(@PathVariable String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) return ResponseEntity.notFound().build();
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
    @PostMapping("{electionId}")
    public ResponseEntity<Election> loadElection(@PathVariable String electionId,
                                                 @RequestParam(required = false) String folderName) {
        Election election = electionService.readResults(electionId, folderName != null ? folderName : electionId);
        if (election == null) return ResponseEntity.status(500).build();
        return ResponseEntity.ok(election);
    }

    @PostMapping("{electionId}/candidatelists")
    public ResponseEntity<Election> loadCandidateLists(
            @PathVariable String electionId,
            @RequestParam(required = false) String folderName) {

        String folder = folderName != null ? folderName : electionId;

        Election election = new Election(electionId);

        //  fill this instance
        electionService.loadCandidateLists(election, folder);

        return ResponseEntity.ok(election);
    }

}
