package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// Service for handling national election data and seat calculations.

@Service
public class NationalService {
    
    private static final int TOTAL_SEATS = 150;
    
    // Calculates seat allocations
    public Map<String, Integer> calculateSeatsDHondt(Election election) {
        // Collect vote totals per party by aggregating from ALL municipalities
        // This ensures we get the complete vote count, not just from National records
        Map<String, Integer> votesPerParty = new HashMap<>();
        Map<String, String> partyNames = new HashMap<>(); // Store party names for reference
        
        // Aggregate votes from all municipalities across all constituencies
        for (nl.hva.ict.sm3.backend.model.Constituency constituency : election.getConstituencies()) {
            for (nl.hva.ict.sm3.backend.model.Municipality municipality : constituency.getMunicipalities()) {
                for (nl.hva.ict.sm3.backend.model.Party party : municipality.getAllParties()) {
                    String partyId = party.getId();
                    int votes = party.getVotes();
                    votesPerParty.merge(partyId, votes, Integer::sum);
                    // Store party name if not already stored
                    if (!partyNames.containsKey(partyId)) {
                        partyNames.put(partyId, party.getName());
                    }
                }
            }
        }
        
        if (votesPerParty.isEmpty()) {
            System.out.println("Warning: No valid votes found for seat calculation");
            return new HashMap<>();
        }
        
        // Calculate total valid votes
        long totalValidVotes = votesPerParty.values().stream()
                .mapToLong(Integer::longValue)
                .sum();
        
        //  Calculate electoral threshold
        long threshold = totalValidVotes / TOTAL_SEATS;
        
        System.out.println("Total valid votes: " + totalValidVotes);
        System.out.println("Electoral threshold: " + threshold);
        System.out.println("Parties participating: " + votesPerParty.size());
        
        // Filter parties
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
        
        // Step 5: Generate quotients
        List<Quotient> allQuotients = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : eligibleParties.entrySet()) {
            String partyId = entry.getKey();
            int votes = entry.getValue();

            for (int divisor = 1; divisor <= TOTAL_SEATS; divisor++) {
                double quotient = (double) votes / divisor;
                allQuotients.add(new Quotient(partyId, quotient));
            }
        }
        
        // Sort quotients in descending order (highest first)
        allQuotients.sort((a, b) -> Double.compare(b.value, a.value));
        
        // Allocate seats to the top 150 quotients
        Map<String, Integer> seatAllocations = new HashMap<>();
        
        for (int i = 0; i < TOTAL_SEATS && i < allQuotients.size(); i++) {
            String partyId = allQuotients.get(i).partyId;
            seatAllocations.merge(partyId, 1, Integer::sum);
        }
        
        // Log results
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
     * Also recalculates vote totals from municipalities to ensure accuracy.
     * Since National objects are immutable, this replaces them with new objects.
     * 
     * @param election The election to update
     * @param seatAllocations Map of partyId -> number of seats
     */
    public void updateNationalRecordsWithSeats(Election election, Map<String, Integer> seatAllocations) {
        // First, recalculate vote totals from all municipalities to ensure accuracy
        Map<String, Integer> recalculatedVotes = new HashMap<>();
        Map<String, String> partyNames = new HashMap<>();
        
        // Aggregate votes from all municipalities
        for (nl.hva.ict.sm3.backend.model.Constituency constituency : election.getConstituencies()) {
            for (nl.hva.ict.sm3.backend.model.Municipality municipality : constituency.getMunicipalities()) {
                for (nl.hva.ict.sm3.backend.model.Party party : municipality.getAllParties()) {
                    String partyId = party.getId();
                    recalculatedVotes.merge(partyId, party.getVotes(), Integer::sum);
                    if (!partyNames.containsKey(partyId)) {
                        partyNames.put(partyId, party.getName());
                    }
                }
            }
        }

        election.setSeatAllocations(seatAllocations);

        // Update or create National records with correct vote totals and seat counts
        List<National> currentNationals = new ArrayList<>(election.getNationalVotes());
        Set<String> updatedPartyIds = new HashSet<>();
        
        for (National old : currentNationals) {
            String partyId = old.getPartyId();
            int seats = seatAllocations.getOrDefault(partyId, 0);
            int validVotes = recalculatedVotes.getOrDefault(partyId, 0);
            
            // Only update records that have votes or seats
            if (validVotes > 0 || seats > 0) {
                National updated = National.forCombined(
                        old.getId(),
                        old.getElectionId(),
                        old.getElectionName(),
                        old.getPartyId(),
                        old.getPartyName(),
                        old.getShortCode(),
                        validVotes, // Use recalculated votes
                        old.getRejectedVotes(),
                        old.getTotalCounted(),
                        seats,
                        old.getType()
                );
                election.replaceNationalVote(old.getId(), updated);
                updatedPartyIds.add(partyId);
            }
        }
        
        // Create National records for parties that have votes/seats but no National record yet
        for (Map.Entry<String, Integer> entry : recalculatedVotes.entrySet()) {
            String partyId = entry.getKey();
            if (!updatedPartyIds.contains(partyId)) {
                int validVotes = entry.getValue();
                int seats = seatAllocations.getOrDefault(partyId, 0);
                
                if (validVotes > 0 || seats > 0) {
                    String electionId = election.getId();
                    String nationalId = String.format("%s-%s-PARTY_VOTES", electionId, partyId);
                    National newNational = National.forCombined(
                            nationalId,
                            electionId,
                            "Tweede Kamer",
                            partyId,
                            partyNames.getOrDefault(partyId, "Unknown Party"),
                            null,
                            validVotes,
                            0,
                            0,
                            seats,
                            null
                    );
                    election.addNationalVotes(newNational);
                }

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
    
    // Helper class

    private static class Quotient {
        final String partyId;
        final double value;
        
        Quotient(String partyId, double value) {
            this.partyId = partyId;
            this.value = value;
        }
    }
}

