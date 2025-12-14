package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "municipalities")
public class Municipality {
    @Id
    private String id;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "valid_votes")
    private int validVotes;
    
    @ElementCollection
    @CollectionTable(name = "municipality_party_votes", joinColumns = @JoinColumn(name = "municipality_id"))
    @MapKeyColumn(name = "party_id")
    @Column(name = "votes")
    private Map<String, Integer> partyVotes = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "municipality_party_names", joinColumns = @JoinColumn(name = "municipality_id"))
    @MapKeyColumn(name = "party_id")
    @Column(name = "party_name")
    private Map<String, String> partyNames = new HashMap<>();
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "municipality_id")
    private List<PollingStation> pollingStations = new ArrayList<>();

    // Default constructor for JPA
    protected Municipality() {}

    /**
        * Constructs a new Municipality.
     *
             * @param id         the unique ID of the municipality
     * @param name       the display name of the municipality
     * @param validVotes initial number of valid votes
     */
    public Municipality(String id, String name, int validVotes) {
        this.id = id;
        this.name = name;
        this.validVotes = validVotes;
    }
    /** @return the municipality ID */
    public String getId() { return id; }
    /** @return the municipality name */
    public String getName() { return name; }
    /** @return the total number of valid votes cast in the municipality */

    public int getValidVotes() { return validVotes; }

    /**
     * Adds a vote count for a specific political party.
     * If the party already exists, votes are added to the existing count.
     * The party name is also updated or inserted.
     *
     * @param partyId   unique identifier of the party
     * @param partyName the human-readable name of the party
     * @param votes     number of votes received by the party
     */
    public void addVotesForParty(String partyId, String partyName, int votes) {
        partyVotes.put(partyId, partyVotes.getOrDefault(partyId, 0) + votes);
        partyNames.put(partyId, partyName);
        validVotes += votes;
    }
    public void addPollingStation(PollingStation ps) {
        pollingStations.add(ps);
    }

    public PollingStation getPollingStationById(String id) {
        return pollingStations.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public List<PollingStation> getPollingStations() {
        return pollingStations;
    }


    /**
     * Returns a list of all parties in this municipality, sorted by number of votes in descending order.
     *
     * @return a sorted list of {@link Party} objects
     */
    public List<Party> getAllParties() {
        return partyVotes.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .map(e -> new Party(
                        e.getKey(),
                        partyNames.get(e.getKey()),
                        e.getValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Municipality{id='%s', name='%s', validVotes=%d}".formatted(id, name, validVotes);
    }
}
