package nl.hva.ict.sm3.backend.model;

public class Municipality {
    private String name;
    private int validVotes;

    public Municipality(String name, int validVotes) {
        this.name = name;
        this.validVotes = validVotes;
    }

    public String getName() { return name; }
    public int getValidVotes() { return validVotes; }

    @Override
    public String toString() {
        return "Municipality{name='%s', validVotes=%d}".formatted(name, validVotes);
    }
}
