package nl.hva.ict.sm3.backend.model;

public class VoteResult {

    private String type; // "party" or "candidate"
    private String id;
    private int votes;

    public VoteResult(String type, String id, int votes) {
        this.type = type;
        this.id = id;
        this.votes = votes;
    }

    public String getType() { return type; }
    public String getId() { return id; }
    public int getVotes() { return votes; }
}
