package nl.hva.ict.sm3.backend.model;

public class Municipality {
    private String id;
    private String name;
    private int validVotes;

    public Municipality(String id,String name, int validVotes) {
        this.id = id;
        this.name = name;
        this.validVotes = validVotes;
    }

    public String getId() {
        return id;
    }

    public String getName() { return name; }
    public int getValidVotes() { return validVotes; }

    @Override
    public String toString() {
        return "Municipality{id='%s, name='%s', validVotes=%d}".formatted(id, name, validVotes);
    }
}
