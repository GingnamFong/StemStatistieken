package nl.hva.ict.sm3.backend.model;

/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 */
public class National {
    private final String id;
    private final String votingMethod;
    private final int maxVotes;
    private final int uncountedVotes;
    private final int validVotes;
    private final int totalVotes;


    public National(String id, String votingMethod, int maxVotes, int uncountedVotes, int validVotes, int totalVotes ) {
        this.id = id;
        this.votingMethod = votingMethod;
        this.maxVotes = maxVotes;
        this.uncountedVotes = uncountedVotes;
        this.validVotes = validVotes;
        this.totalVotes = totalVotes;
    }

    @Override
    public String toString() {
        return "You have to create a proper election model yourself!";
    }

    public String getId() {
        return id;
    }
}