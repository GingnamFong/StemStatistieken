package nl.hva.ict.sm3.backend.model;

public class National {
    private String id;
    private String name;
    private String region;

    public National (String id,String name, String region) {
        this .id = id;
        this.name = name;
        this.region = region;
    }

    public National() {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}