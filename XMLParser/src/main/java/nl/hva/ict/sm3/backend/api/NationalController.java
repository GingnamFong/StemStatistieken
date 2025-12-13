package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.dto.NationalDto;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import nl.hva.ict.sm3.backend.service.NationalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/elections/{electionId}/national")
public class NationalController {
    
    private final DutchElectionService electionService;
    private final NationalService nationalService;

    public NationalController(DutchElectionService electionService, NationalService nationalService) {
        this.electionService = electionService;
        this.nationalService = nationalService;
    }

    /*
      GET /elections/{electionId}/national/results
      Returns national vote results sections:
      Link for national results: GET http://localhost:8081/elections/TK2023/national/results
     */
    @GetMapping("/results")
    public ResponseEntity<List<NationalDto>> getNationalResults(@PathVariable String electionId) {
        Election election = getOrLoadElection(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<NationalDto> response = election.getNationalVotes().stream()
                .map(NationalDto::from)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /*
      POST /elections/{electionId}/national/calculate-seats
      Endpoint for seats calculations
      Link for seats data: POST http://localhost:8081/elections/TK2023/national/calculate-seats
     */
    @PostMapping("/calculate-seats")
    public ResponseEntity<Map<String, Integer>> calculateSeats(@PathVariable String electionId) {
        Election election = getOrLoadElection(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Calculate seats
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);
        
        // Update the National records with calculated seats
        nationalService.updateNationalRecordsWithSeats(election, seatAllocations);
        
        return ResponseEntity.ok(seatAllocations);
    }

    /*
      GET /elections/{electionId}/national/seats
      Returns the current seat allocation per party as a map.
      If seats haven't been calculated yet, returns an empty map.
      Example link: GET http://localhost:8081/elections/TK2023/national/seats
     */
    @GetMapping("/seats")
    public ResponseEntity<Map<String, Integer>> getSeatAllocations(@PathVariable String electionId) {
        Election election = getOrLoadElection(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Integer> seatAllocations = nationalService.getSeatAllocations(election);
        return ResponseEntity.ok(seatAllocations);
    }

    /*
      GET /elections/{electionId}/national/results-with-seats
      Returns national results with seats already calculated.
      If seats haven't been calculated, it will calculate them first.
      Link with party data and seats: GET http://localhost:8081/elections/TK2023/national/results-with-seats
     */
    @GetMapping("/results-with-seats")
    public ResponseEntity<List<NationalDto>> getNationalResultsWithSeats(@PathVariable String electionId) {
        Election election = getOrLoadElection(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Check if seats have been calculated
        Map<String, Integer> currentSeats = nationalService.getSeatAllocations(election);
        if (currentSeats.isEmpty() || currentSeats.values().stream().allMatch(seats -> seats == 0)) {
            // Calculate seats if not done yet
            Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);
            nationalService.updateNationalRecordsWithSeats(election, seatAllocations);
        }
        
        List<NationalDto> response = election.getNationalVotes().stream()
                .map(NationalDto::from)
                .toList();
        
        return ResponseEntity.ok(response);
    }
    
    // Helper class
    private Election getOrLoadElection(String electionId) {
        // Only get from database/cache, don't parse XML
        return electionService.getElectionById(electionId);
    }
}
