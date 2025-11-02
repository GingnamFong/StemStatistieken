package nl.hva.ict.sm3.backend.model;

/**
 * Represents a political party, either nationally or locally (per municipality).
 */
public class Party {
    private String id;
    private String name;
    private int votes; // can represent total votes nationally or local votes

    public Party(String id, String name, int votes) {
        this.id = id;
        this.name = name;
        this.votes = votes;
    }

    public Party(String id, String name) {
        this(id, name, 0);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getVotes() { return votes; }

    public void addVotes(int votes) {
        this.votes += votes;
    }

    @Override
    public String toString() {
        return "Party{id='%s', name='%s', votes=%d}".formatted(id, name, votes);
    }
}
