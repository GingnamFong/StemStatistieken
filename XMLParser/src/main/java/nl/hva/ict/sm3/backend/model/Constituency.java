package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.List;

public class Constituency {
    private String id;
    private String name;
    private List<Municipality> municipalities = new ArrayList<>();
    private int totalVotes = 0;



    public Constituency(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void addMunicipality(Municipality m) {
        municipalities.add(m);
        totalVotes += m.getValidVotes(); // sum votes
    }
    public Municipality getMunicipalityById(String municipalityId) {
        for (Municipality m : municipalities) {
            if (m.getId().equals(municipalityId)) {
                return m;
            }
        }
        return null;
    }
    public void addToTotalVotes(int votes) {
        this.totalVotes += votes;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    public List<Municipality> getMunicipalities() { return municipalities; }
    public int getTotalVotes() { return totalVotes; }


    @Override
    public String toString() {
        return "Constituency{id='%s', name='%s', totalVotes=%d, municipalities=%s}".formatted(
                id, name, totalVotes, municipalities
        );
    }
}
