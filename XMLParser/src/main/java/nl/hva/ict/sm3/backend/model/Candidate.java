package nl.hva.ict.sm3.backend.model;

public class Candidate {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String initials;
    private final String residence;
    private final String partyId;
    private final String partyName;
	private final int candidateIdentifier;
    private String shortCode; // ("YeşilgözD")
    private int votes;

	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int candidateIdentifier) {
        this(id, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier, null, 0);
    }

	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int candidateIdentifier, int votes) {
        this(id, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier, null, votes);
    }

	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int candidateIdentifier, String shortCode, int votes) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.residence = residence;
        this.partyId = partyId;
        this.partyName = partyName;
		this.candidateIdentifier = candidateIdentifier;
        this.shortCode = shortCode;
        this.votes = votes;
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

	public int getCandidateIdentifier() {
		return candidateIdentifier;
	}

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void addVotes(int votes) {
        this.votes += votes;
    }

    @Override
    public String toString() {
        return "Candidate{" +
                "id='" + id + '\'' +
                ", name='" + firstName + " " + lastName + '\'' +
                ", initials='" + initials + '\'' +
                ", residence='" + residence + '\'' +
                ", party='" + partyName + '\'' +
				", candidateIdentifier=" + candidateIdentifier +
                ", votes=" + votes +
                '}';
    }
}
