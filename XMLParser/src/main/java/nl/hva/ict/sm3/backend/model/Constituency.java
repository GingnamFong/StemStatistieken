package nl.hva.ict.sm3.backend.model;

public class Constituency {
    private String id;
    private String name;

    // You can add more attributes later, such as total votes, party results, etc.

    public Constituency(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Constituency{id='%s', name='%s'}".formatted(id, name);
    }

}
