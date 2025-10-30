package nl.hva.ict.sm3.backend.model;

public class PartyResult {
    private String id;
    private String name;
    private int votes;

    public PartyResult(String id, String name, int votes) {
        this.id = id;
        this.name = name;
        this.votes = votes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getVotes() { return votes; }
}
