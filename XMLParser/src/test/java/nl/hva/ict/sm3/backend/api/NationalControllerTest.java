package nl.hva.ict.sm3.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hva.ict.sm3.backend.dto.NationalDto;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import nl.hva.ict.sm3.backend.service.NationalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.test.context.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NationalController.
 * 
 * These tests use MockMvc to test REST endpoints without starting a full server.
 * We mock the service dependencies to isolate controller logic.
 * 
 * Testing approach:
 * - Mock DutchElectionService and NationalService
 * - Use MockMvc to simulate HTTP requests
 * - Verify response status, content type, and JSON structure
 */
@WebMvcTest(NationalController.class)
class NationalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @nl.hva.ict.sm3.backend.api.MockitoBean
    private DutchElectionService electionService;

    @nl.hva.ict.sm3.backend.api.MockitoBean
    private NationalService nationalService;

    private Election testElection;
    private List<National> testNationals;

    @BeforeEach
    void setUp() {
        testElection = new Election("TK2023");
        
        // Create test National records
        testNationals = Arrays.asList(
                createNational("1", "VVD", 1_589_519),
                createNational("2", "D66", 656_292),
                createNational("3", "PVV", 2_450_000)
        );
        
        testNationals.forEach(testElection::addNationalVotes);
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results - Should return grouped national results")
    void testGetNationalResults_Success() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/results"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].electionInfo", notNullValue()))
                .andExpect(jsonPath("$[0].partyInfo", notNullValue()))
                .andExpect(jsonPath("$[0].seatsData", notNullValue()))
                .andExpect(jsonPath("$[0].rejectedData", notNullValue()))
                .andExpect(jsonPath("$[0].electionInfo.electionId", is("TK2023")))
                .andExpect(jsonPath("$[0].partyInfo.partyId", is("1")))
                .andExpect(jsonPath("$[0].partyInfo.partyName", is("VVD")));

        verify(electionService, times(1)).getElectionById("TK2023");
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results - Should load election if not in cache")
    void testGetNationalResults_AutoLoad() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(null);
        when(electionService.readResults("TK2023", "TK2023")).thenReturn(testElection);

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(electionService, times(1)).getElectionById("TK2023");
        verify(electionService, times(1)).readResults("TK2023", "TK2023");
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results - Should return 404 when election not found")
    void testGetNationalResults_NotFound() throws Exception {
        // Arrange
        when(electionService.getElectionById("UNKNOWN")).thenReturn(null);
        when(electionService.readResults("UNKNOWN", "UNKNOWN")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/elections/UNKNOWN/national/results"))
                .andExpect(status().isNotFound());

        verify(electionService, times(1)).getElectionById("UNKNOWN");
        verify(electionService, times(1)).readResults("UNKNOWN", "UNKNOWN");
    }

    @Test
    @DisplayName("POST /elections/{electionId}/national/calculate-seats - Should calculate and return seat allocations")
    void testCalculateSeats_Success() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);
        
        Map<String, Integer> seatAllocations = new HashMap<>();
        seatAllocations.put("1", 24);
        seatAllocations.put("2", 10);
        seatAllocations.put("3", 37);
        
        when(nationalService.calculateSeatsDHondt(testElection)).thenReturn(seatAllocations);
        doNothing().when(nationalService).updateNationalRecordsWithSeats(eq(testElection), any());

        // Act & Assert
        mockMvc.perform(post("/elections/TK2023/national/calculate-seats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$", isA(Map.class)))
                .andExpect(jsonPath("$.1", is(24)))
                .andExpect(jsonPath("$.2", is(10)))
                .andExpect(jsonPath("$.3", is(37)));

        verify(nationalService, times(1)).calculateSeatsDHondt(testElection);
        verify(nationalService, times(1)).updateNationalRecordsWithSeats(eq(testElection), eq(seatAllocations));
    }

    @Test
    @DisplayName("POST /elections/{electionId}/national/calculate-seats - Should return 404 when election not found")
    void testCalculateSeats_NotFound() throws Exception {
        // Arrange
        when(electionService.getElectionById("UNKNOWN")).thenReturn(null);
        when(electionService.readResults("UNKNOWN", "UNKNOWN")).thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/elections/UNKNOWN/national/calculate-seats"))
                .andExpect(status().isNotFound());

        verify(nationalService, never()).calculateSeatsDHondt(any());
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/seats - Should return current seat allocations")
    void testGetSeatAllocations_Success() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);
        
        Map<String, Integer> seatAllocations = new HashMap<>();
        seatAllocations.put("1", 24);
        seatAllocations.put("2", 10);
        
        when(nationalService.getSeatAllocations(testElection)).thenReturn(seatAllocations);

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/seats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect((ResultMatcher) jsonPath("$", isA(Map.class)))
                .andExpect(jsonPath("$.1", is(24)))
                .andExpect(jsonPath("$.2", is(10)));

        verify(nationalService, times(1)).getSeatAllocations(testElection);
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results-with-seats - Should calculate seats if not done")
    void testGetNationalResultsWithSeats_CalculateIfNeeded() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);
        
        Map<String, Integer> emptySeats = new HashMap<>();
        Map<String, Integer> calculatedSeats = new HashMap<>();
        calculatedSeats.put("1", 24);
        calculatedSeats.put("2", 10);
        calculatedSeats.put("3", 37);
        
        when(nationalService.getSeatAllocations(testElection)).thenReturn(emptySeats);
        when(nationalService.calculateSeatsDHondt(testElection)).thenReturn(calculatedSeats);
        doNothing().when(nationalService).updateNationalRecordsWithSeats(eq(testElection), any());

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/results-with-seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(nationalService, times(1)).getSeatAllocations(testElection);
        verify(nationalService, times(1)).calculateSeatsDHondt(testElection);
        verify(nationalService, times(1)).updateNationalRecordsWithSeats(eq(testElection), any());
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results-with-seats - Should not recalculate if seats already exist")
    void testGetNationalResultsWithSeats_UseExistingSeats() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);
        
        Map<String, Integer> existingSeats = new HashMap<>();
        existingSeats.put("1", 24);
        existingSeats.put("2", 10);
        
        when(nationalService.getSeatAllocations(testElection)).thenReturn(existingSeats);

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/results-with-seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        verify(nationalService, times(1)).getSeatAllocations(testElection);
        verify(nationalService, never()).calculateSeatsDHondt(any());
        verify(nationalService, never()).updateNationalRecordsWithSeats(any(), any());
    }

    @Test
    @DisplayName("GET /elections/{electionId}/national/results-with-seats - Should recalculate if all seats are zero")
    void testGetNationalResultsWithSeats_RecalculateIfAllZero() throws Exception {
        // Arrange
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);
        
        Map<String, Integer> zeroSeats = new HashMap<>();
        zeroSeats.put("1", 0);
        zeroSeats.put("2", 0);
        
        Map<String, Integer> calculatedSeats = new HashMap<>();
        calculatedSeats.put("1", 24);
        calculatedSeats.put("2", 10);
        
        when(nationalService.getSeatAllocations(testElection)).thenReturn(zeroSeats);
        when(nationalService.calculateSeatsDHondt(testElection)).thenReturn(calculatedSeats);
        doNothing().when(nationalService).updateNationalRecordsWithSeats(eq(testElection), any());

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/national/results-with-seats"))
                .andExpect(status().isOk());

        verify(nationalService, times(1)).calculateSeatsDHondt(testElection);
        verify(nationalService, times(1)).updateNationalRecordsWithSeats(eq(testElection), any());
    }

    /**
     * Helper method to create a National record for testing
     */
    private National createNational(String partyId, String partyName, int validVotes) {
        String id = String.format("TK2023-%s-PARTY_VOTES", partyId);
        return National.forCombined(
                id,
                "TK2023",
                "Tweede Kamer der Staten-Generaal 2023",
                partyId,
                partyName,
                null,
                validVotes,
                0,
                0,
                0,
                null
        );
    }
}

