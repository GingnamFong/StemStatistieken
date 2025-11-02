package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
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

    @GetMapping("/{electionId}/municipalities/{municipalityId}")
    public ResponseEntity<Municipality> getMunicipalityById(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {

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

        Election election = new Election(electionId);

        //  fill this instance
        electionService.loadCandidateLists(election, folder);

        return ResponseEntity.ok(election);
    }

}
