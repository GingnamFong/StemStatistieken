package nl.hva.ict.sm3.backend.model;


import java.util.*;
import java.util.stream.Collectors;

public class Municipality {
    private String id;
    private String name;
    private int validVotes;
    private Map<String, Integer> partyVotes = new HashMap<>();
    private Map<String, String> partyNames = new HashMap<>();
    private List<PollingStation> pollingStations = new ArrayList<>();

    public Municipality(String id, String name, int validVotes) {
        this.id = id;
        this.name = name;
        this.validVotes = validVotes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getValidVotes() { return validVotes; }

    public void addVotesForParty(String partyId, String partyName, int votes) {
        partyVotes.put(partyId, partyVotes.getOrDefault(partyId, 0) + votes);
        partyNames.put(partyId, partyName);
        validVotes += votes;
    }

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

    public List<Party> getTopParties(int n) {
        return getAllParties().stream()
                .limit(n)
                .collect(Collectors.toList());
    }
    public void addPollingStation(PollingStation station) {
        pollingStations.add(station);
        validVotes += station.getTotalVotes();
    }

    public List<PollingStation> getPollingStations() {
        return pollingStations;
    }

    public PollingStation getPollingStationByPostalCode(String postalCode) {
        return pollingStations.stream()
                .filter(s -> s.getPostalCode().equalsIgnoreCase(postalCode))
                .findFirst()
                .orElse(null);
    }


    @Override
    public String toString() {
        return "Municipality{id='%s', name='%s', validVotes=%d}".formatted(id, name, validVotes);
    }
}
