package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NationalService.
 * 
 * These tests verify:
 * - D'Hondt seat calculation algorithm
 * - Seat allocation updates to National records
 * - Edge cases (empty data, threshold filtering, etc.)
 */
class NationalServiceTest {

    private NationalService nationalService;
    private Election election;

    @BeforeEach
    void setUp() {
        nationalService = new NationalService();
        election = new Election("TEST2023");
    }

    @Test
    @DisplayName("Should calculate seats correctly using D'Hondt method")
    void testCalculateSeatsDHondt_BasicScenario() {
        // Arrange: Create test data with known vote counts
        // Party 1: 1,000,000 votes
        // Party 2: 500,000 votes
        // Party 3: 250,000 votes
        // Total: 1,750,000 votes
        // With 150 seats, threshold = 1,750,000 / 150 = ~11,667 votes
        
        election.addNationalVotes(createNational("1", "Party1", 1_000_000));
        election.addNationalVotes(createNational("2", "Party2", 500_000));
        election.addNationalVotes(createNational("3", "Party3", 250_000));

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        assertNotNull(seatAllocations);
        assertFalse(seatAllocations.isEmpty());
        
        // Verify all parties got seats
        assertTrue(seatAllocations.containsKey("1"));
        assertTrue(seatAllocations.containsKey("2"));
        assertTrue(seatAllocations.containsKey("3"));
        
        // Verify seat distribution is proportional (Party1 should have most seats)
        int seats1 = seatAllocations.get("1");
        int seats2 = seatAllocations.get("2");
        int seats3 = seatAllocations.get("3");
        
        assertTrue(seats1 > seats2, "Party1 should have more seats than Party2");
        assertTrue(seats2 > seats3, "Party2 should have more seats than Party3");
        
        // Verify total seats allocated = 150
        int totalSeats = seatAllocations.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        assertEquals(150, totalSeats, "Total seats should equal 150");
    }

    @Test
    @DisplayName("Should filter out parties below electoral threshold")
    void testCalculateSeatsDHondt_ThresholdFiltering() {
        // Arrange: Create parties where one is below threshold
        // Total votes: 1,500,000
        // Threshold: 1,500,000 / 150 = 10,000 votes
        // Party 1: 1,000,000 votes (above threshold)
        // Party 2: 5,000 votes (below threshold - should be excluded)
        
        election.addNationalVotes(createNational("1", "Party1", 1_000_000));
        election.addNationalVotes(createNational("2", "Party2", 5_000));

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        assertTrue(seatAllocations.containsKey("1"), "Party1 should get seats");
        assertFalse(seatAllocations.containsKey("2"), "Party2 should be excluded (below threshold)");
        
        // All 150 seats should go to Party1
        assertEquals(150, seatAllocations.get("1"), "Party1 should get all 150 seats");
    }

    @Test
    @DisplayName("Should return empty map when no valid votes exist")
    void testCalculateSeatsDHondt_NoValidVotes() {
        // Arrange: Election with no national votes
        // (election is empty)

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        assertNotNull(seatAllocations);
        assertTrue(seatAllocations.isEmpty(), "Should return empty map when no votes exist");
    }

    @Test
    @DisplayName("Should return empty map when all parties are below threshold")
    void testCalculateSeatsDHondt_AllPartiesBelowThreshold() {
        // Arrange: All parties have very few votes
        election.addNationalVotes(createNational("1", "Party1", 100));
        election.addNationalVotes(createNational("2", "Party2", 50));

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        assertTrue(seatAllocations.isEmpty(), "Should return empty map when all parties below threshold");
    }

    @Test
    @DisplayName("Should update National records with calculated seats")
    void testUpdateNationalRecordsWithSeats() {
        // Arrange
        National national1 = createNational("1", "Party1", 1_000_000);
        National national2 = createNational("2", "Party2", 500_000);
        
        election.addNationalVotes(national1);
        election.addNationalVotes(national2);
        
        Map<String, Integer> seatAllocations = new HashMap<>();
        seatAllocations.put("1", 100);
        seatAllocations.put("2", 50);

        // Act
        nationalService.updateNationalRecordsWithSeats(election, seatAllocations);

        // Assert
        List<National> updatedNationals = election.getNationalVotes();
        National updated1 = updatedNationals.stream()
                .filter(n -> n.getPartyId().equals("1"))
                .findFirst()
                .orElse(null);
        National updated2 = updatedNationals.stream()
                .filter(n -> n.getPartyId().equals("2"))
                .findFirst()
                .orElse(null);

        assertNotNull(updated1);
        assertNotNull(updated2);
        assertEquals(100, updated1.getNumberOfSeats(), "Party1 should have 100 seats");
        assertEquals(50, updated2.getNumberOfSeats(), "Party2 should have 50 seats");
    }

    @Test
    @DisplayName("Should get seat allocations from Election model")
    void testGetSeatAllocations() {
        // Arrange
        election.addNationalVotes(createNational("1", "Party1", 1_000_000));
        election.addNationalVotes(createNational("2", "Party2", 500_000));
        
        Map<String, Integer> expectedSeats = new HashMap<>();
        expectedSeats.put("1", 100);
        expectedSeats.put("2", 50);
        election.setSeatAllocations(expectedSeats);

        // Act
        Map<String, Integer> result = nationalService.getSeatAllocations(election);

        // Assert
        assertEquals(expectedSeats, result);
        assertEquals(100, result.get("1"));
        assertEquals(50, result.get("2"));
    }

    @Test
    @DisplayName("Should calculate seats from National records if not stored in Election")
    void testGetSeatAllocations_FromNationalRecords() {
        // Arrange: National records already have seat counts
        National national1 = National.forCombined(
                "id1", "TEST2023", "Test Election", "1", "Party1", null,
                1_000_000, 0, 0, 100, null
        );
        National national2 = National.forCombined(
                "id2", "TEST2023", "Test Election", "2", "Party2", null,
                500_000, 0, 0, 50, null
        );
        
        election.addNationalVotes(national1);
        election.addNationalVotes(national2);
        // Don't set seatAllocations in Election

        // Act
        Map<String, Integer> result = nationalService.getSeatAllocations(election);

        // Assert
        assertEquals(100, result.get("1"));
        assertEquals(50, result.get("2"));
    }

    @Test
    @DisplayName("Should handle single party getting all seats")
    void testCalculateSeatsDHondt_SingleParty() {
        // Arrange: Only one party with votes
        election.addNationalVotes(createNational("1", "Party1", 1_000_000));

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        assertEquals(1, seatAllocations.size());
        assertEquals(150, seatAllocations.get("1"), "Single party should get all 150 seats");
    }

    @Test
    @DisplayName("Should handle multiple National records for same party")
    void testCalculateSeatsDHondt_MultipleRecordsSameParty() {
        // Arrange: Same party appears in multiple National records
        // This shouldn't happen in practice, but we should handle it gracefully
        election.addNationalVotes(createNational("1", "Party1", 500_000));
        election.addNationalVotes(createNational("1", "Party1", 500_000)); // Duplicate

        // Act
        Map<String, Integer> seatAllocations = nationalService.calculateSeatsDHondt(election);

        // Assert
        // Votes should be summed (1,000,000 total)
        assertTrue(seatAllocations.containsKey("1"));
        // Should get all 150 seats since it's the only party
        assertEquals(150, seatAllocations.get("1"));
    }

    /**
     * Helper method to create a National record for testing
     */
    private National createNational(String partyId, String partyName, int validVotes) {
        String id = String.format("TEST-%s-PARTY_VOTES", partyId);
        return National.forCombined(
                id,
                "TEST2023",
                "Test Election",
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

