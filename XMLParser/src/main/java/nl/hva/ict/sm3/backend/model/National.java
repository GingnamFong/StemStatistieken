package nl.hva.ict.sm3.backend.model;

public class National {
    private String id;
    private String party;
    private String national;

    public National (String id,String name, String national) {
        this.id = id;
        this.party = name;
        this.national = national;
    }

    public String getId() { return id; }
    public String getParty() { return party; }
    public String getNational() { return national; }

}