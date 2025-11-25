package nl.hva.ict.sm3.backend.model;

import java.util.*;

public class PollingStation {
    private String id;
    private String name;
    private String postalCode;
    private int validVotes;
    private Map<String, Integer> partyVotes = new HashMap<>();
    private Map<String, String> partyNames = new HashMap<>();

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


