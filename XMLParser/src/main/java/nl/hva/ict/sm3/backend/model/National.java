package nl.hva.ict.sm3.backend.model;

/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 */
public class National {
    private final String id;
    private final String electionId;
    private final String electionName;
    private final String partyId;
    private final String partyName;
    private final String shortCode;
    private final int validVotes;
    private final int rejectedVotes;
    private final int totalCounted;
    private final int numberOfSeats;

    public National(String id, String electionId, String electionName,
                       String partyId, String partyName, String shortCode,
                       int validVotes, int rejectedVotes, int totalCounted,
                       int numberOfSeats) {
        this.id = id;
        this.electionId = electionId;
        this.electionName = electionName;
        this.partyId = partyId;
        this.partyName = partyName;
        this.shortCode = shortCode;
        this.validVotes = validVotes;
        this.rejectedVotes = rejectedVotes;
        this.totalCounted = totalCounted;
        this.numberOfSeats = numberOfSeats;
    }

    public String getId() {
        return id;
    }

    public String getElectionId() {
        return electionId;
    }

    public String getElectionName() {
        return electionName;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public String getShortCode() {
        return shortCode;
    }

    public int getValidVotes() {
        return validVotes;
    }

    public int getRejectedVotes() {
        return rejectedVotes;
    }

    public int getTotalCounted() {
        return totalCounted;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    @Override
    public String toString() {
        return "PartyResult{" +
                "id='" + id + '\'' +
                ", election='" + electionName + '\'' +
                ", partyName='" + partyName + '\'' +
                ", shortCode='" + shortCode + '\'' +
                ", validVotes=" + validVotes +
                ", rejectedVotes=" + rejectedVotes +
                ", totalCounted=" + totalCounted +
                ", numberOfSeats=" + numberOfSeats +
                '}';
    }
}

/*{
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

    public String getId() {
        return id;
    }

    public String getVotingMethod() {
        return votingMethod;
    }

    public int getMaxVotes() {
        return maxVotes;
    }

    public int getUncountedVotes() {
        return uncountedVotes;
    }

    public int getValidVotes() {
        return validVotes;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    @Override
    public String toString() {
        return "Votes{" +
                "id='" + id + '\'' +
                ", votingMethod='" + votingMethod + '\'' +
                ", maxVotes=" + maxVotes +
                ", uncountedVotes=" + uncountedVotes +
                ", validVotes=" + validVotes +
                ", totalVotes=" + totalVotes +
                '}';
    }
} */