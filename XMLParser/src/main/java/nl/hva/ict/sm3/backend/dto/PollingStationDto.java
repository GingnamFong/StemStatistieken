package nl.hva.ict.sm3.backend.dto;

import java.util.List;

public class PollingStationDto {
    private String id;
    private String name;
    private String postalCode;
    private int validVotes;
    private List<PartyDto> partyResults;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public int getValidVotes() {
        return validVotes;
    }

    public void setValidVotes(int validVotes) {
        this.validVotes = validVotes;
    }

    public List<PartyDto> getPartyResults() {
        return partyResults;
    }

    public void setPartyResults(List<PartyDto> partyResults) {
        this.partyResults = partyResults;
    }
}