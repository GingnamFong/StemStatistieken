package nl.hva.ict.sm3.backend.model;

/**
 * Represents a political party, which can exist at a national level or
 * at the municipality level depending on context.
 * <p>
 * A party contains an identifier, a name, and a vote count.
 * The vote count may represent national totals or local municipality votes.
 */
public class Party {
    private String id;
    private String name;
    private int votes; // can represent total votes nationally or local votes

    /**
     * Constructs a Party with an ID, name, and initial vote count.
     *
     * @param id    the unique party ID
     * @param name  the display name of the party
     * @param votes initial number of votes for the party
     */
    public Party(String id, String name, int votes) {
        this.id = id;
        this.name = name;
        this.votes = votes;
    }

    /**
     * Constructs a Party with an ID and name, assuming zero initial votes.
     *
     * @param id   the unique party ID
     * @param name the display name of the party
     */
    public Party(String id, String name) {
        this(id, name, 0);
    }
    /** @return the unique party ID */
    public String getId() { return id; }
    /** @return the name of the party */
    public String getName() { return name; }
    /** @return the number of votes the party has received */
    public int getVotes() { return votes; }

    /**
     * Adds a number of votes to the party's current total.
     *
     * @param votes the number of votes to add
     */
    public void addVotes(int votes) {
        this.votes += votes;
    }
    /**
     * Returns a string representation of the party.
     *
     * @return a formatted string containing ID, name, and vote count
     */
    @Override
    public String toString() {
        return "Party{id='%s', name='%s', votes=%d}".formatted(id, name, votes);
    }
}
