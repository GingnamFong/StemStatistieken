package nl.hva.ict.sm3.backend.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Municipality {
    private String id;
    private String name;
    private int validVotes;
    private Map<String, Integer> partyVotes = new HashMap<>();
    private Map<String, String> partyNames = new HashMap<>();

    public Municipality(String id,String name, int validVotes) {
        this.id = id;
        this.name = name;
        this.validVotes = validVotes;
        this.partyVotes = new HashMap<>();
    }

    public String getId() {
        return id;
    }
    public void addVotesForParty(String partyId, String partyName, int votes) {
        partyVotes.put(partyId, partyVotes.getOrDefault(partyId, 0) + votes);
        partyNames.put(partyId, partyName);
        validVotes += votes;
    }
    public List<Map<String, Object>> getTopPartiesWithNames(int n) {
        return partyVotes.entrySet()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(n)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", e.getKey());
                    m.put("name", partyNames.get(e.getKey()));
                    m.put("votes", e.getValue());
                    return m;
                })
                .toList();
    }

    public String getName() { return name; }
    public int getValidVotes() { return validVotes; }

    @Override
    public String toString() {
        return "Municipality{id='%s, name='%s', validVotes=%d}".formatted(id, name, validVotes);
    }
}
