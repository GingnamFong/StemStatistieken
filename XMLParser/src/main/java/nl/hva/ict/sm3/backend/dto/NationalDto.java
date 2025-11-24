package nl.hva.ict.sm3.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.hva.ict.sm3.backend.model.National;

/**
 * DTO that groups national vote data into logical sections for nicer JSON output.
 */
public class NationalDto {

    @JsonProperty("electionInfo")
    private final ElectionInfo electionInfo;

    @JsonProperty("partyInfo")
    private final PartyInfo partyInfo;

    @JsonProperty("seatsData")
    private final SeatsData seatsData;

    @JsonProperty("rejectedData")
    private final RejectedData rejectedData;

    public NationalDto(ElectionInfo electionInfo,
                       PartyInfo partyInfo,
                       SeatsData seatsData,
                       RejectedData rejectedData) {
        this.electionInfo = electionInfo;
        this.partyInfo = partyInfo;
        this.seatsData = seatsData;
        this.rejectedData = rejectedData;
    }

    public static NationalDto from(National national) {
        ElectionInfo electionSection = new ElectionInfo(national.getElectionId(), national.getElectionName());
        PartyInfo partySection = new PartyInfo(national.getPartyId(), national.getPartyName());
        SeatsData seatsSection = new SeatsData(national.getValidVotes(), national.getNumberOfSeats());
        RejectedData rejectedSection = new RejectedData(national.getRejectedVotes(), national.getTotalCounted());
        return new NationalDto(electionSection, partySection, seatsSection, rejectedSection);
    }

    public ElectionInfo getElectionInfo() {
        return electionInfo;
    }

    public PartyInfo getPartyInfo() {
        return partyInfo;
    }

    public SeatsData getSeatsData() {
        return seatsData;
    }

    public RejectedData getRejectedData() {
        return rejectedData;
    }

    public static class ElectionInfo {
        private final String electionId;
        private final String electionName;

        public ElectionInfo(String electionId, String electionName) {
            this.electionId = electionId;
            this.electionName = electionName;
        }

        public String getElectionId() {
            return electionId;
        }

        public String getElectionName() {
            return electionName;
        }
    }

    public static class PartyInfo {
        private final String partyId;
        private final String partyName;

        public PartyInfo(String partyId, String partyName) {
            this.partyId = partyId;
            this.partyName = partyName;
        }

        public String getPartyId() {
            return partyId;
        }

        public String getPartyName() {
            return partyName;
        }
    }

    public static class SeatsData {
        private final int validVotes;
        private final int numberOfSeats;

        public SeatsData(int validVotes, int numberOfSeats) {
            this.validVotes = validVotes;
            this.numberOfSeats = numberOfSeats;
        }

        public int getValidVotes() {
            return validVotes;
        }

        public int getNumberOfSeats() {
            return numberOfSeats;
        }
    }

    public static class RejectedData {
        private final int rejectedVotes;
        private final int totalCounted;

        public RejectedData(int rejectedVotes, int totalCounted) {
            this.rejectedVotes = rejectedVotes;
            this.totalCounted = totalCounted;
        }

        public int getRejectedVotes() {
            return rejectedVotes;
        }

        public int getTotalCounted() {
            return totalCounted;
        }
    }
}
