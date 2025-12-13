package nl.hva.ict.sm3.backend.dto;

import java.util.List;
import java.util.Map;

public class ElectionDto {
    private String id;
    private List<ConstituencyDto> constituencies;
    private List<PartyDto> parties;
    private List<CandidateDto> candidates;
    private List<NationalDto> nationalVotes;
    private Map<String, Integer> seatAllocations;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ConstituencyDto> getConstituencies() {
        return constituencies;
    }

    public void setConstituencies(List<ConstituencyDto> constituencies) {
        this.constituencies = constituencies;
    }

    public List<PartyDto> getParties() {
        return parties;
    }

    public void setParties(List<PartyDto> parties) {
        this.parties = parties;
    }

    public List<CandidateDto> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<CandidateDto> candidates) {
        this.candidates = candidates;
    }

    public List<NationalDto> getNationalVotes() {
        return nationalVotes;
    }

    public void setNationalVotes(List<NationalDto> nationalVotes) {
        this.nationalVotes = nationalVotes;
    }

    public Map<String, Integer> getSeatAllocations() {
        return seatAllocations;
    }

    public void setSeatAllocations(Map<String, Integer> seatAllocations) {
        this.seatAllocations = seatAllocations;
    }
}