package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import java.util.Objects;

/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 */

@Entity
@Table(name = "national_results")
public class National {
    @Id
    private String id;
    
    // This column is managed by JPA via the @JoinColumn on Election.nationalVotes
    // We mark it as insertable/updatable = false to avoid conflicts
    @Column(name = "election_id", insertable = false, updatable = false)
    private String electionId;
    
    @Column(name = "election_name")
    private String electionName;
    
    @Column(name = "party_id")
    private String partyId;
    
    @Column(name = "party_name")
    private String partyName;
    
    @Column(name = "short_code")
    private String shortCode;

    @Column(name = "valid_votes")
    private int validVotes; // wachten op oplossing
    
    @Column(name = "rejected_votes")
    private int rejectedVotes; // verplaatsen naar election.java
    
    @Column(name = "total_counted")
    private int totalCounted; // verplaatsen naar election.java
    
    @Column(name = "number_of_seats")
    private int numberOfSeats; // wachten op oplossing
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NationalResult type;

    // Default constructor for JPA
    protected National() {}

    public National(String id,
                    String electionId,
                    String electionName,
                    String partyId,
                    String partyName,
                    String shortCode,

                    int validVotes,
                    int rejectedVotes,
                    int totalCounted,
                    int numberOfSeats,
                    NationalResult type) {
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
        this.type = type;
    }

    public static National forCombined(String id, String electionId, String electionName,
                                         String partyId, String partyName, String shortCode,
                                         int validVotes) {
        return new National(id, electionId, electionName, partyId, partyName, shortCode,
                validVotes, 0, 0, 0, NationalResult.PARTY_VOTES);
    }

    public static National forRejectedData(String id, String electionId, String electionName,
                                           String partyId, String partyName, String shortCode,
                                           int rejectedVotes, int totalCounted) {
        return new National(id, electionId, electionName, partyId, partyName, shortCode,
                0, rejectedVotes, totalCounted, 0, NationalResult.REJECTED_DATA);
    }

    public static National forSeats(String id, String electionId, String electionName,
                                    String partyId, String partyName, String shortCode,
                                    int numberOfSeats) {
        return new National(id, electionId, electionName, partyId, partyName, shortCode,
                0, 0, 0, numberOfSeats, NationalResult.SEATS);
    }

    public static National forCombined(String id,
                                       String electionId,
                                       String electionName,
                                       String partyId,
                                       String partyName,
                                       String shortCode,
                                       int validVotes,
                                       int rejectedVotes,
                                       int totalCounted,
                                       int numberOfSeats,
                                       NationalResult type) {
        return new National(id, electionId, electionName, partyId, partyName, shortCode,
                validVotes, rejectedVotes, totalCounted, numberOfSeats, type);
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

    public NationalResult getType() {
        return type;
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
                ", type=" + type +
                '}';


    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof National)) return false;
        National national = (National) o;
        return Objects.equals(id, national.id) && type == national.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
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