package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an election in the system.
 * 
 * <p>This entity stores election data including constituencies, parties, candidates,
 * and national votes. Elections are linked to multiple related entities through
 * one-to-many relationships.</p>
 * 
 * <p>Performance optimizations:
 * <ul>
 *   <li>Lazy loading - Related entities (constituencies, parties, candidates, national votes)
 *       are loaded only when accessed</li>
 *   <li>Caching - Entity is cached at the second level to reduce database queries</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "elections")
@Cacheable
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class Election {
    @Id
    private String id;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "election_id")
    private List<Constituency> constituencies = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "election_id")
    @MapKey(name = "id")
    private Map<String, Party> parties = new HashMap<>();
    
    /**
     * One-to-many relationship with Candidate.
     * An election contains multiple candidates from different parties.
     * 
     * <p>Performance optimizations:
     * <ul>
     *   <li>Cascade operations are set to ALL, meaning that when an election is deleted,
     *       all associated candidates will also be deleted</li>
     *   <li>Fetch type is LAZY to improve performance by loading candidates only when needed</li>
     *   <li>Orphan removal is enabled to automatically remove candidates when they are no longer
     *       associated with an election</li>
     * </ul>
     * </p>
     * 
     * <p>Lazy loading strategy: The candidates collection is not loaded from the database
     * until it is explicitly accessed, reducing initial query overhead and memory usage.
     * This prevents N+1 query problems and improves application performance.</p>
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "election_id")
    private List<Candidate> candidates = new ArrayList<>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "election_id")
    private List<National> nationalVotes = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "seat_allocations", joinColumns = @JoinColumn(name = "election_id"))
    @MapKeyColumn(name = "party_id")
    @Column(name = "seats")
    private Map<String, Integer> seatAllocations = new HashMap<>();


    // Default constructor for JPA
    protected Election() {}
    
    public Election(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public List<Constituency> getConstituencies() { return constituencies; }
    public List<Candidate> getCandidates() { return candidates; }
    
    // Return actual list for read operations but as unmodifiable for safety
    public List<Party> getParties() { return new ArrayList<>(parties.values()); }
    
    // Return actual list reference so modifications work
    public List<National> getNationalVotes() { return nationalVotes; }
    
    // Method to get a copy if needed
    public List<National> getNationalVotesCopy() { return new ArrayList<>(nationalVotes); }

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

    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void addParty(Party party) {
        parties.put(party.getId(), party);
    }

    public void addNationalVotes(National national) {
        nationalVotes.add(national);
    }
    
    /**
     * Clears all national votes. Use this before re-adding filtered votes.
     */
    public void clearNationalVotes() {
        nationalVotes.clear();
    }
    
    /**
     * Replaces a National vote record with the same ID.
     * Used when updating seat counts.
     */
    public void replaceNationalVote(String nationalId, National updatedNational) {
        for (int i = 0; i < nationalVotes.size(); i++) {
            if (nationalVotes.get(i).getId().equals(nationalId)) {
                nationalVotes.set(i, updatedNational);
                return;
            }
        }
        // If not found, just add it
        nationalVotes.add(updatedNational);
    }
    
    /**
     * Sets the seat allocations for parties.
     */
    public void setSeatAllocations(Map<String, Integer> seatAllocations) {
        this.seatAllocations = new HashMap<>(seatAllocations);
    }
    
    /**
     * Gets the seat allocations for parties.
     */
    public Map<String, Integer> getSeatAllocations() {
        return new HashMap<>(seatAllocations);
    }
    
    /**
     * Gets the number of seats for a specific party.
     */
    public int getSeatsForParty(String partyId) {
        return seatAllocations.getOrDefault(partyId, 0);
    }

    public Constituency getConstituencyById(String id) {
        return constituencies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Party getPartyById(String partyId) {
        return parties.get(partyId);
    }

    public List<Party> getTopParties(int n) {
        return parties.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getVotes(), p1.getVotes()))
                .limit(n)
                .toList();
    }

    public List<Municipality> getAllMunicipalities() {
        return constituencies.stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .toList();
    }

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
        return "Election{id='%s', constituencies=%d, parties=%d, candidates=%d, nationalVotes=%d}"
                .formatted(id, constituencies.size(), parties.size(), candidates.size(), nationalVotes.size());
    }
}
