package nl.hva.ict.sm3.backend.model;

public class National {
    private String id;
    private String party;
    private String region;

    public National (String id,String name, String region) {
        this.id = id;
        this.party = name;
        this.region = region;
    }

    public String getId() { return id; }
    public String getParty() { return party; }
    public String getRegion() { return region; }

    @Override
    public String toString() {
        return "Party{id='%s', name='%s', region=%s}".formatted(id, party, region);
    }
}