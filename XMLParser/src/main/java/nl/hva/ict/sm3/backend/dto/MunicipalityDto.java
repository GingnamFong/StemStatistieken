package nl.hva.ict.sm3.backend.dto;

import java.util.List;

public class MunicipalityDto {
    private String id;
    private String name;
    private int validVotes;
    private List<PollingStationDto> pollingStations;
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

    public int getValidVotes() {
        return validVotes;
    }

    public void setValidVotes(int validVotes) {
        this.validVotes = validVotes;
    }

    public List<PollingStationDto> getPollingStations() {
        return pollingStations;
    }

    public void setPollingStations(List<PollingStationDto> pollingStations) {
        this.pollingStations = pollingStations;
    }

    public List<PartyDto> getPartyResults() {
        return partyResults;
    }

    public void setPartyResults(List<PartyDto> partyResults) {
        this.partyResults = partyResults;
    }
}