package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for handling national election data and seat calculations.
 *
 */
@Service
public class NationalService {
    
    private static final int TOTAL_SEATS = 150; // Tweede Kamer has 150 seats
    
    /**
     * Calculates seat allocations using the D'Hondt method.
     * 
     * The D'Hondt method works by:
     * 1. For each party, calculate quotients: votes/1, votes/2, votes/3, etc.
     * 2. Sort all quotients from all parties in descending order
     * 3. Allocate seats to the parties with the 150 highest quotients
     * 
     * @param election The election containing national vote data
     * @return Map of partyId -> number of seats allocated
     */
    public Map<String, Integer> calculateSeatsDHondt(Election election) {
        // Step 1: Collect vote totals per party from National records
        Map<String, Integer> votesPerParty = new HashMap<>();
        
        for (National national : election.getNationalVotes()) {
            // Only use records with valid votes (PARTY_VOTES type or combined records)
            if (national.getValidVotes() > 0) {
                String partyId = national.getPartyId();
                // Sum up votes if a party appears multiple times (shouldn't happen, but be safe)
                votesPerParty.merge(partyId, national.getValidVotes(), Integer::sum);
            }
        }
        
        if (votesPerParty.isEmpty()) {
            System.out.println("Warning: No valid votes found for seat calculation");
            return new HashMap<>();
        }
        
        // Step 2: Calculate total valid votes (for threshold check)
        long totalValidVotes = votesPerParty.values().stream()
                .mapToLong(Integer::longValue)
                .sum();
        
        // Step 3: Calculate electoral threshold (1 full seat worth of votes)
        // In practice, parties need at least totalValidVotes / TOTAL_SEATS votes
        long threshold = totalValidVotes / TOTAL_SEATS;
        
        System.out.println("Total valid votes: " + totalValidVotes);
        System.out.println("Electoral threshold: " + threshold);
        System.out.println("Parties participating: " + votesPerParty.size());
        
        // Step 4: Filter parties that meet the threshold
        Map<String, Integer> eligibleParties = votesPerParty.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
        
        if (eligibleParties.isEmpty()) {
            System.out.println("Warning: No parties meet the electoral threshold");
            return new HashMap<>();
        }
        
        System.out.println("Eligible parties (meet threshold): " + eligibleParties.size());
        
        // Step 5: Generate quotients for D'Hondt method
        // For each party, create quotients: votes/1, votes/2, votes/3, ... votes/TOTAL_SEATS
        List<Quotient> allQuotients = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : eligibleParties.entrySet()) {
            String partyId = entry.getKey();
            int votes = entry.getValue();
            
            // Generate quotients: votes/1, votes/2, votes/3, ... votes/TOTAL_SEATS
            for (int divisor = 1; divisor <= TOTAL_SEATS; divisor++) {
                double quotient = (double) votes / divisor;
                allQuotients.add(new Quotient(partyId, quotient));
            }
        }
        
        // Step 6: Sort quotients in descending order (highest first)
        allQuotients.sort((a, b) -> Double.compare(b.value, a.value));
        
        // Step 7: Allocate seats to the top 150 quotients
        Map<String, Integer> seatAllocations = new HashMap<>();
        
        for (int i = 0; i < TOTAL_SEATS && i < allQuotients.size(); i++) {
            String partyId = allQuotients.get(i).partyId;
            seatAllocations.merge(partyId, 1, Integer::sum);
        }
        
        // Step 8: Log results
        System.out.println("\n=== Seat Allocation Results ===");
        seatAllocations.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(entry -> {
                    int votes = eligibleParties.get(entry.getKey());
                    System.out.printf("Party %s: %d seats (from %d votes)%n", 
                            entry.getKey(), entry.getValue(), votes);
                });
        
        int totalAllocated = seatAllocations.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        System.out.println("Total seats allocated: " + totalAllocated);
        
        return seatAllocations;
    }
    
    /**
     * Updates all National records in the election with calculated seat counts.
     * Since National objects are immutable, this replaces them with new objects.
     * 
     * @param election The election to update
     * @param seatAllocations Map of partyId -> number of seats
     */
    public void updateNationalRecordsWithSeats(Election election, Map<String, Integer> seatAllocations) {
        List<National> updatedNationals = new ArrayList<>();
        
        for (National national : election.getNationalVotes()) {
            String partyId = national.getPartyId();
            int seats = seatAllocations.getOrDefault(partyId, 0);
            
            // Create a new National object with updated seat count
            National updated = National.forCombined(
                    national.getId(),
                    national.getElectionId(),
                    national.getElectionName(),
                    national.getPartyId(),
                    national.getPartyName(),
                    national.getShortCode(),
                    national.getValidVotes(),
                    national.getRejectedVotes(),
                    national.getTotalCounted(),
                    seats,
                    national.getType()
            );
            
            updatedNationals.add(updated);
        }
        
        // Replace the entire list (we need to clear and re-add since Election doesn't have a setter)
        // Actually, we need to check if Election has a method to replace the list
        // For now, we'll store the seat allocations in the Election model
        election.setSeatAllocations(seatAllocations);
        
        // Update individual records by finding and replacing them
        List<National> currentNationals = election.getNationalVotes();
        for (int i = 0; i < currentNationals.size(); i++) {
            National old = currentNationals.get(i);
            String partyId = old.getPartyId();
            int seats = seatAllocations.getOrDefault(partyId, 0);
            
            if (old.getNumberOfSeats() != seats) {
                // Replace with updated version
                National updated = National.forCombined(
                        old.getId(),
                        old.getElectionId(),
                        old.getElectionName(),
                        old.getPartyId(),
                        old.getPartyName(),
                        old.getShortCode(),
                        old.getValidVotes(),
                        old.getRejectedVotes(),
                        old.getTotalCounted(),
                        seats,
                        old.getType()
                );
                // We'll need to add a method to Election to replace a National record
                election.replaceNationalVote(old.getId(), updated);
            }
        }
    }
    
    /**
     * Gets the current seat allocations for an election.
     * 
     * @param election The election
     * @return Map of partyId -> number of seats
     */
    public Map<String, Integer> getSeatAllocations(Election election) {
        Map<String, Integer> seatAllocations = election.getSeatAllocations();
        
        // If not stored, calculate from National records
        if (seatAllocations == null || seatAllocations.isEmpty()) {
            seatAllocations = new HashMap<>();
            for (National national : election.getNationalVotes()) {
                if (national.getNumberOfSeats() > 0) {
                    seatAllocations.put(national.getPartyId(), national.getNumberOfSeats());
                }
            }
        }
        
        return seatAllocations;
    }
    
    /**
     * Helper class to represent a quotient (votes/divisor) for D'Hondt calculation
     */
    private static class Quotient {
        final String partyId;
        final double value;
        
        Quotient(String partyId, double value) {
            this.partyId = partyId;
            this.value = value;
        }
    }
}
