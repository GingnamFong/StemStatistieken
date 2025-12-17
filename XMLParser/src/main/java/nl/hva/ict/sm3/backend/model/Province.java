package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Domain model for a province.
 * Contains name, constituencies and election results (parties with votes).
 */
public class Province {
    private final String name;
    private List<String> constituencyIds = new ArrayList<>();
    private Map<String, Party> parties = new HashMap<>();
    private int totalVotes = 0;

    public Province(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getConstituencyIds() {
        // Returns a copy of the constituency IDs
        return new ArrayList<>(constituencyIds);
    }

    public void addConstituencyId(String constituencyId) {
        // Adds a constituency ID if it doesn't already exist
        if (!constituencyIds.contains(constituencyId)) {
            constituencyIds.add(constituencyId);
        }
    }

    /**
     * Aggregates party votes from constituencies. Call this method to build up the province results.
     */
    public void addPartyVotes(String partyId, String partyName, int votes) {
        // Aggregates votes for a party within this province
        Party existingParty = parties.get(partyId);
        if (existingParty != null) {
            existingParty.addVotes(votes);
        } else {
            parties.put(partyId, new Party(partyId, partyName, votes));
        }
    }
    
    /**
     * Calculates total votes from all parties. Should be called after aggregating all party votes.
     */
    public void calculateTotalVotes() {
        // Calculates total number of votes in the province
        totalVotes = parties.values().stream()
                .mapToInt(Party::getVotes)
                .sum();
    }

    public List<Party> getParties() {
        // Returns all parties sorted by votes (descending)
        return parties.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getVotes(), p1.getVotes()))
                .collect(Collectors.toList());
    }

    public List<Party> getTopParties(int n) {
        // Returns the top N parties
        return getParties().stream()
                .limit(n)
                .collect(Collectors.toList());
    }

    public Party getPartyById(String partyId) {
        // Finds a party by ID
        return parties.get(partyId);
    }

    public int getTotalVotes() {
        // Returns total number of votes
        return totalVotes;
    }

    /**
     * Checks if this province contains a specific constituency.
     * Uses O(1) lookup via Map lookup in parties or direct list check.
     * 
     * @param constituencyId the constituency ID to check
     * @return true if the constituency exists in this province
     */
    public boolean hasConstituency(String constituencyId) {
        // Checks if the province contains a specific constituency
        return constituencyIds.contains(constituencyId);
    }
    
    /**
     * Returns the number of constituencies in this province.
     * 
     * @return the count of constituencies
     */
    public int getConstituencyCount() {
        // Returns the number of constituencies
        return constituencyIds.size();
    }

    @Override
    public String toString() {
        return "Province{name='%s', constituencies=%d, parties=%d, totalVotes=%d}"
                .formatted(name, constituencyIds.size(), parties.size(), totalVotes);
    }
}

