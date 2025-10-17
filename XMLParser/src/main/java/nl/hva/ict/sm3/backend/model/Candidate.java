package nl.hva.ict.sm3.backend.model;

public class Candidate {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String initials;
    private final String residence;
    private final String partyId;
    private final String partyName;

    public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.residence = residence;
        this.partyId = partyId;
        this.partyName = partyName;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getInitials() {
        return initials;
    }

    public String getResidence() {
        return residence;
    }

    public String getPartyId() {
        return partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "id='" + id + '\'' +
                ", name='" + firstName + " " + lastName + '\'' +
                ", initials='" + initials + '\'' +
                ", residence='" + residence + '\'' +
                ", party='" + partyName + '\'' +
                '}';
    }
}
