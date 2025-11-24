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

/**
 * Controller for handling national election votes and seat calculations.
 * 
 * This controller provides REST endpoints to:
 * - Get national vote results grouped by election info, party info, seats data, and rejected data
 * - Calculate seat allocations using the D'Hondt method
 * - Get seat allocation results per party
 * 
 * What is a Controller?
 * A controller in Spring Boot is a class that handles HTTP requests from clients (like your frontend).
 * It acts as a bridge between the web layer (HTTP) and your business logic (services).
 * 
 * Key annotations:
 * - @RestController: Tells Spring this class handles HTTP requests and automatically converts responses to JSON
 * - @RequestMapping: Sets the base URL path for all endpoints in this controller
 * - @GetMapping: Handles GET requests (reading data)
 * - @PostMapping: Handles POST requests (creating/updating data)
 * - @PathVariable: Extracts values from the URL path (e.g., /elections/TK2023 -> electionId = "TK2023")
 */
@RestController
@RequestMapping("/elections/{electionId}/national")
public class NationalController {
    
    private final DutchElectionService electionService;
    private final NationalService nationalService;
    
    /**
     * Constructor - Spring automatically provides these services (dependency injection)
     */
    public NationalController(DutchElectionService electionService, NationalService nationalService) {
        this.electionService = electionService;
        this.nationalService = nationalService;
    }
    
    /**
     * GET /elections/{electionId}/national/results
     * 
     * Returns national vote results grouped into organized sections:
     * - electionInfo: electionId, electionName
     * - partyInfo: partyId, partyName
     * - seatsData: validVotes, numberOfSeats
     * - rejectedData: rejectedVotes, totalCounted
     * 
     * Example: GET http://localhost:8081/elections/TK2023/national/results
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
    
    /**
     * POST /elections/{electionId}/national/calculate-seats
     * 
     * Calculates seat allocations for all parties using the D'Hondt method.
     * The D'Hondt method is the proportional representation system used in Dutch elections.
     * 
     * This endpoint:
     * 1. Reads all national vote totals from the parsed XML data
     * 2. Applies the D'Hondt algorithm to distribute 150 seats proportionally
     * 3. Updates the numberOfSeats field in each National record
     * 4. Returns a map showing partyId -> number of seats allocated
     * 
     * Example: POST http://localhost:8081/elections/TK2023/national/calculate-seats
     */
    @PostMapping("/calculate-seats")
    public ResponseEntity<Map<String, Integer>> calculateSeats(@PathVariable String electionId) {
        Election election = getOrLoadElection(electionId);
        if (election == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Calculate seats using D'Hondt method
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);
        
        // Update the National records with calculated seats
        nationalService.updateNationalRecordsWithSeats(election, seatAllocations);
        
        return ResponseEntity.ok(seatAllocations);
    }
    
    /**
     * GET /elections/{electionId}/national/seats
     * 
     * Returns the current seat allocation per party as a simple map.
     * If seats haven't been calculated yet, returns an empty map.
     * 
     * Example: GET http://localhost:8081/elections/TK2023/national/seats
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
    
    /**
     * GET /elections/{electionId}/national/results-with-seats
     * 
     * Returns national results with seats already calculated.
     * If seats haven't been calculated, it will calculate them first.
     * 
     * Example: GET http://localhost:8081/elections/TK2023/national/results-with-seats
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
    
    /**
     * Helper method to get election from cache or load it if not present
     */
    private Election getOrLoadElection(String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            election = electionService.readResults(electionId, electionId);
        }
        return election;
    }
}
