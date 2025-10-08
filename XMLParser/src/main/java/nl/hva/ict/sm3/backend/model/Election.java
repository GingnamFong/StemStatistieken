package nl.hva.ict.sm3.backend.model;
import java.util.ArrayList;

import java.util.List;

/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 */
public class Election {
    private final String id;
    private List<Constituency> constituencies = new ArrayList<>();

    public Election(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<Constituency> getConstituencies() {
        return constituencies;
    }

    public void addConstituency(Constituency newConstituency) {
        Constituency existing = getConstituencyById(newConstituency.getId());
        if (existing != null) {
            // merge municipalities — totalVotes updates automatically
            for (Municipality m : newConstituency.getMunicipalities()) {
                existing.addMunicipality(m);
            }
        } else {
            constituencies.add(newConstituency);
        }
    }

    @Override
    public String toString() {
        return "Election{id='%s', constituencies=%s}".formatted(id, constituencies);
    }

    public Constituency getConstituencyById(String id) {
        return constituencies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
