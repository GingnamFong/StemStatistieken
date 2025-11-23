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
    private final int validVotes; // wachten op oplossing
    private final int rejectedVotes; // verplaatsen naar election.java
    private final int totalCounted; // verplaatsen naar election.java
    private final int numberOfSeats; // wachten op oplossing

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

/*
public class National {
    private final String id;
    private final ElectionInfo election;
    private final PartyInfo party;
    private final VoteTotals voteTotals;
    private final RejectedData rejectedData;

    public National(String id, ElectionInfo election, PartyInfo party,
                    VoteTotals voteTotals, RejectedData rejectedData) {
        this.id = id;
        this.election = election;
        this.party = party;
        this.voteTotals = voteTotals;
        this.rejectedData = rejectedData;
    }

    public String getId() { return id; }
    public ElectionInfo getElection() { return election; }
    public PartyInfo getParty() { return party; }
    public VoteTotals getVoteTotals() { return voteTotals; }
    public RejectedData getRejectedData() { return rejectedData; }

    @Override
    public String toString() {
        return "National{" +
                "id='" + id + '\'' +
                ", election=" + election +
                ", party=" + party +
                ", voteTotals=" + voteTotals +
                ", rejectedData=" + rejectedData +
                '}';
    }
}
 */