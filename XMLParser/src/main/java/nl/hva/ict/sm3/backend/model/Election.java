package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Represents an election, which consists of multiple constituencies,
 * political parties, and candidates.
 * <p>
 * The Election class acts as a central container for organizing election data,
 * including parties, candidates, and the hierarchical structure of constituencies
 * and their municipalities.
 */

public class Election {
    private final String id;
    private List<Constituency> constituencies = new ArrayList<>();
    private Map<String, Party> parties = new HashMap<>();
    private List<Candidate> candidates = new ArrayList<>();

    /**
     * Constructs a new Election with the given ID.
     *
     * @param id the unique identifier for the election
     */
    public Election(String id) {
        this.id = id;
    }
    /** @return the unique ID of the election */
    public String getId() { return id; }
    /** @return the list of constituencies in the election */
    public List<Constituency> getConstituencies() { return constituencies; }
    /** @return the list of candidates participating in the election */
    public List<Candidate> getCandidates() { return candidates; }

    /**
     * Adds a constituency to the election.
     * If a constituency with the same ID already exists, the new municipalities
     * are merged into the existing one.
     *
     * @param newConstituency the constituency to add
     */
    public void addConstituency(Constituency newConstituency) {
        Constituency existing = getConstituencyById(newConstituency.getId());
        if (existing != null) {
            for (Municipality m : newConstituency.getMunicipalities()) {
                existing.addMunicipality(m);
            }
        } else {
            constituencies.add(newConstituency);
        }
    }

    /**
     * Adds a candidate to the election.
     *
     * @param candidate the candidate to add
     */
    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }
    /**
            * Adds a party to the election.
            * If a party with the same ID already exists, it will be overwritten.
            *
            * @param party the party to add
     */

    public void addParty(Party party) {
        parties.put(party.getId(), party);
    }

    /**
     * Retrieves a constituency by its ID.
     *
     * @param id the ID of the constituency
     * @return the matching constituency, or {@code null} if not found
     */
    public Constituency getConstituencyById(String id) {
        return constituencies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Retrieves a party by its ID.
     *
     * @param partyId the ID of the party
     * @return the matching party, or {@code null} if not found
     */
    public Party getPartyById(String partyId) {
        return parties.get(partyId);
    }

    /**
     * Returns the top N parties with the highest number of votes.
     *
     * @param n the number of top parties to return
     * @return a list of top N parties sorted by descending vote count
     */
    public List<Party> getTopParties(int n) {
        return parties.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getVotes(), p1.getVotes()))
                .limit(n)
                .toList();
    }
    /**
     * Retrieves all municipalities across all constituencies.
     *
     * @return a flattened list of all municipalities
     */
    public List<Municipality> getAllMunicipalities() {
        return constituencies.stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .toList();
    }
    /**
     * Finds a municipality by its ID.
     *
     * @param municipalityId the ID of the municipality
     * @return the matching municipality, or {@code null} if not found
     */
    public Municipality getMunicipalityById(String municipalityId) {
        return getAllMunicipalities().stream()
                .filter(m -> m.getId().equals(municipalityId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Finds a candidate by their ID.
     */
    public Candidate getCandidateById(String candidateId) {
        if (candidateId == null || candidateId.trim().isEmpty()) {
            return null;
        }
        return candidates.stream()
                .filter(c -> c.getId() != null && c.getId().equals(candidateId))
                .findFirst()
                .orElse(null);
    }


    /**
     * Finds a candidate by matching shortCode from votes file with lastName + all initials.
     * Format: lastName + all initials without dots (e.g. "YeşilgözD" or "JettenRAA" for "R.A.A.")
     */
    public Candidate getCandidateByShortCode(String shortCode) {
        if (shortCode == null || shortCode.trim().isEmpty()) {
            return null;
        }
        
        String trimmedShortCode = shortCode.trim();
        
        for (Candidate candidate : candidates) {
            String lastName = candidate.getLastName();
            String initials = candidate.getInitials();
            
            if (lastName == null || lastName.trim().isEmpty()) {
                continue;
            }
            
            // Extract all letters from initials (remove dots, spaces, etc.)
            String allInitials = null;
            if (initials != null && !initials.trim().isEmpty()) {
                // Remove all non-letter characters (dots, spaces, etc.) and keep only letters
                allInitials = initials.replaceAll("[^A-Za-z]", "");
            }
            
            if (allInitials != null && !allInitials.isEmpty()) {
                String constructedCode = lastName.trim() + allInitials;
                if (trimmedShortCode.equalsIgnoreCase(constructedCode)) {
                    return candidate;
                }
            }
        }
        
        return null;
    }

    @Override
    public String toString() {
        return "Election{id='%s', constituencies=%d, parties=%d, candidates=%d}"
                .formatted(id, constituencies.size(), parties.size(), candidates.size());
    }
}
