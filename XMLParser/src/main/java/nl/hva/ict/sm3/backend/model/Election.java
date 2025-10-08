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

    public void addConstituency(Constituency constituency) {
        constituencies.add(constituency);
    }

    @Override
    public String toString() {
        return "Election{id='%s', constituencies=%s}".formatted(id, constituencies);
    }
}
