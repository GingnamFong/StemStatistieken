package nl.hva.ict.sm3.backend.model;

import java.util.List;

/**
 *
 */
public class MunicipalitySummary {
    private String id;
    private String name;
    private int validVotes;
    private List<PartyResult> topParties;

    public MunicipalitySummary(String id, String name, int validVotes, List<PartyResult> topParties) {
        this.id = id;
        this.name = name;
        this.validVotes = validVotes;
        this.topParties = topParties;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getValidVotes() { return validVotes; }
    public List<PartyResult> getTopParties() { return topParties; }
}
