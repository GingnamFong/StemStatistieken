package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Election {
    private final String id;
    private List<Constituency> constituencies = new ArrayList<>();
    private Map<String, Party> parties = new HashMap<>();
    private List<Candidate> candidates = new ArrayList<>();
    private List<National> nationalVotes = new ArrayList<>();


    public Election(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public List<Constituency> getConstituencies() { return constituencies; }
    public List<Candidate> getCandidates() { return candidates; }
    public List<Party> getParties() { return new ArrayList<>(parties.values()); }
    public List<National> getNationalVotes() { return new ArrayList<>(nationalVotes); }

    public void addConstituency(Constituency newConstituency) {
        Constituency existing = getConstituencyById(newConstituency.getId());
        if (existing != null) {
            for (Municipality m : newConstituency.getMunicipalities()) {
                existing.addMunicipality(m);
            }
        } else {
            constituencies.add(newConstituency);
        }
    }

    public void addCandidate(Candidate candidate) {
        candidates.add(candidate);
    }

    public void addParty(Party party) {
        parties.put(party.getId(), party);
    }

    public void addNationalVotes(National national) {};

    public Constituency getConstituencyById(String id) {
        return constituencies.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Party getPartyById(String partyId) {
        return parties.get(partyId);
    }

    public List<Party> getTopParties(int n) {
        return parties.values().stream()
                .sorted((p1, p2) -> Integer.compare(p2.getVotes(), p1.getVotes()))
                .limit(n)
                .toList();
    }

    public List<Municipality> getAllMunicipalities() {
        return constituencies.stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .toList();
    }

    public Municipality getMunicipalityById(String municipalityId) {
        return getAllMunicipalities().stream()
                .filter(m -> m.getId().equals(municipalityId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String toString() {
        return "Election{id='%s', constituencies=%d, parties=%d, candidates=%d}"
                .formatted(id, constituencies.size(), parties.size(), candidates.size(), nationalVotes.size());
    }
}
