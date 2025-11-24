package nl.hva.ict.sm3.backend.model;

import java.util.*;
import java.util.stream.Collectors;

public class Municipality {
    private String id;
    private String name;
    private int validVotes;
    private Map<String, Integer> partyVotes = new HashMap<>();
    private Map<String, String> partyNames = new HashMap<>();

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
