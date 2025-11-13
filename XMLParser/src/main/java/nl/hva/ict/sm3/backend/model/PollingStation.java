package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.List;

public class PollingStation {

    private String id;
    private String name;
    private String postalCode;

    private List<VoteResult> results = new ArrayList<>();

    public PollingStation(String id, String name, String postalCode) {
        this.id = id;
        this.name = name;
        this.postalCode = postalCode;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPostalCode() { return postalCode; }

    public void addResult(VoteResult result) {
        results.add(result);
    }

    public List<VoteResult> getResults() {
        return results;
    }

    public int getTotalVotes() {
        return results.stream()
                .mapToInt(VoteResult::getVotes)
                .sum();
    }
}
