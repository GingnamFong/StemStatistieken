package nl.hva.ict.sm3.backend.model;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This will hold the information for one specific election.<br/>
 * <b>This class is by no means production ready! You need to alter it extensively!</b>
 */
public class Election {
    private final String id;
    private List<Constituency> constituencies = new ArrayList<>();
    private Map<String, Party> parties = new HashMap<>();


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
    public List<Party> getParties() {
        return new ArrayList<>(parties.values());
    }

    public void addParty(Party party) {
        parties.put(party.getId(), party);
    }

    public List<Party> getTopParties(int n) {
        return parties.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getTotalVotes(), p1.getTotalVotes()))
                .limit(n)
                .toList();
    }


    public Party getPartyById(String partyId) {
        return parties.get(partyId);
    }
}
