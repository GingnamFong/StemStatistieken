package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "polling_stations")
public class PollingStation {
    @Id
    private String id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "postal_code")
    private String postalCode;
    
    @Column(name = "valid_votes")
    private int validVotes;
    
    @ElementCollection
    @CollectionTable(name = "polling_station_party_votes", joinColumns = @JoinColumn(name = "station_id"))
    @MapKeyColumn(name = "party_id")
    @Column(name = "votes")
    private Map<String, Integer> partyVotes = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "polling_station_party_names", joinColumns = @JoinColumn(name = "station_id"))
    @MapKeyColumn(name = "party_id")
    @Column(name = "party_name")
    private Map<String, String> partyNames = new HashMap<>();

    // Default constructor for JPA
    protected PollingStation() {}

    public PollingStation(String id, String name, String postalCode) {
        this.id = id;
        this.name = name;
        this.postalCode = postalCode;
    }

    public void addVotes(String partyId, String partyName, int votes) {
        partyVotes.put(partyId, partyVotes.getOrDefault(partyId, 0) + votes);
        partyNames.put(partyId, partyName);
        validVotes += votes;
    }

    public List<Party> getAllParties() {
        return partyVotes.entrySet().stream()
                .map(e -> new Party(e.getKey(), partyNames.get(e.getKey()), e.getValue()))
                .sorted((a, b) -> b.getVotes() - a.getVotes())
                .toList();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPostalCode() { return postalCode; }
    public int getValidVotes() { return validVotes; }
}


