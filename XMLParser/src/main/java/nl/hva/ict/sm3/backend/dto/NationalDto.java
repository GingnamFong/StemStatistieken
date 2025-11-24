package nl.hva.ict.sm3.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;

/**
 * DTO that groups National into nested sections for nicer JSON output.
 */
public class NationalDto {

    @JsonProperty("election data")
    private final ElectionData electionData;

    @JsonProperty("party data")
    private final PartyData partyData;

    @JsonProperty("votes")
    private final VoteInfo votes;

    @JsonProperty("seats")
    private final SeatInfo seats;

    public NationalDto(ElectionData electionData, PartyData partyData, VoteInfo votes, SeatInfo seats) {
        this.electionData = electionData;
        this.partyData = partyData;
        this.votes = votes;
        this.seats = seats;
    }

    // Factory mapper from your model
    public static NationalDto from(National n) {
        ElectionData e = new ElectionData(n.getId(), n.getElectionId(), n.getElectionName());
        PartyData p = new PartyData(n.getPartyId(), n.getPartyName(), n.getShortCode());
        VoteInfo v = new VoteInfo(n.getValidVotes(), n.getRejectedVotes());
        SeatInfo s = new SeatInfo(n.getTotalCounted(), n.getNumberOfSeats(), n.getType());
        return new NationalDto(e, p, v, s);
    }

    // nested DTOs
    public static class ElectionData {
        private final String id;
        private final String electionId;
        private final String electionName;

        public ElectionData(String id, String electionId, String electionName) {
            this.id = id;
            this.electionId = electionId;
            this.electionName = electionName;
        }

        public String getId() { return id; }
        public String getElectionId() { return electionId; }
        public String getElectionName() { return electionName; }
    }

    public static class PartyData {
        private final String partyId;
        private final String partyName;
        private final String shortCode;

        public PartyData(String partyId, String partyName, String shortCode) {
            this.partyId = partyId;
            this.partyName = partyName;
            this.shortCode = shortCode;
        }

        public String getPartyId() { return partyId; }
        public String getPartyName() { return partyName; }
        public String getShortCode() { return shortCode; }
    }

    public static class VoteInfo {
        private final int validVotes;
        private final int rejectedVotes;

        public VoteInfo(int validVotes, int rejectedVotes) {
            this.validVotes = validVotes;
            this.rejectedVotes = rejectedVotes;
        }

        public int getValidVotes() { return validVotes; }
        public int getRejectedVotes() { return rejectedVotes; }
    }

    public static class SeatInfo {
        private final int totalCounted;
        private final int numberOfSeats;
        private final NationalResult type;

        public SeatInfo(int totalCounted, int numberOfSeats, NationalResult type) {
            this.totalCounted = totalCounted;
            this.numberOfSeats = numberOfSeats;
            this.type = type;
        }

        public int getTotalCounted() { return totalCounted; }
        public int getNumberOfSeats() { return numberOfSeats; }
        public NationalResult getType() { return type; }
    }
}
