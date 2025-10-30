package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
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


    @GetMapping("{electionId}/municipalities")
    public ResponseEntity<List<MunicipalitySummary>> getMunicipalitiesSummary(@PathVariable String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) return ResponseEntity.notFound().build();

        List<MunicipalitySummary> summaries = election.getConstituencies()
                .stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .map(m -> new MunicipalitySummary(
                        m.getId(),
                        m.getName(),
                        m.getValidVotes(),
                        m.getTopParties(3) // ✅ directly returns List<PartyResult>
                ))
                .toList();

        return ResponseEntity.ok(summaries);
    }
    @GetMapping("{electionId}/municipalities/{municipalityId}/parties")
    public ResponseEntity<MunicipalitySummary> getAllPartiesForMunicipality(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {

        Election election = electionService.getElectionById(electionId);
        if (election == null) return ResponseEntity.notFound().build();

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return ResponseEntity.notFound().build();

        MunicipalitySummary summary = new MunicipalitySummary(
                municipality.getId(),
                municipality.getName(),
                municipality.getValidVotes(),
                municipality.getAllParties()
        );

        return ResponseEntity.ok(summary);
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
