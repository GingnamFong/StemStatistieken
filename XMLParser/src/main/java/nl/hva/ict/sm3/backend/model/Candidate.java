package nl.hva.ict.sm3.backend.model;

public class Candidate {
    private String id;
    private String name;
    private Party party;
    private int votes;

    public Candidate(String id, String name, Party party) {
        this.id = id;
        this.name = name;
        this.party = party;
        this.votes = 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Party getParty() {
        return party;
    }

    public int getVotes() {
        return votes;
    }

    public void addVotes(int votes) {
        this.votes += votes;
        if (party != null) {
            party.addVotes(votes); // keep total votes in sync with the party
        }
    }

    @Override
    public String toString() {
        return "Candidate{id='%s', name='%s', party='%s', votes=%d}"
                .formatted(id, name, party != null ? party.getName() : "Independent", votes);
    }
}
