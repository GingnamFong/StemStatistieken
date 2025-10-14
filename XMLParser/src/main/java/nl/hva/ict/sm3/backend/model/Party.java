package nl.hva.ict.sm3.backend.model;

public class Party {
    private String id;
    private String name;
    private int totalVotes;

    public Party(String id, String name){
        this.id = id;
        this.name = name;
        this.totalVotes = 0;
    }
    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public int getTotalVotes(){
        return totalVotes;
    }
    public void addVotes(int votes){
        this.totalVotes += votes;
    }
    @Override
    public String toString() {
        return "Party{id='%s', name='%s', totalVotes=%d}".formatted(id, name, totalVotes);
    }
}
