package nl.hva.ict.sm3.backend.model;

public class Candidate {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String initials;
    private final String residence;
    private final String partyId;
    private final String partyName;
	private final int listNumber; // position on the party list (RANKING)
	private final int candidateIdentifier; // value of <CandidateIdentifier Id="...">

	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int listNumber, int candidateIdentifier) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.initials = initials;
        this.residence = residence;
        this.partyId = partyId;
        this.partyName = partyName;
		this.listNumber = listNumber;
		this.candidateIdentifier = candidateIdentifier;
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

	public int getListNumber() {
		return listNumber;
	}

	public int getCandidateIdentifier() {
		return candidateIdentifier;
	}

    @Override
    public String toString() {
        return "Candidate{" +
                "id='" + id + '\'' +
                ", name='" + firstName + " " + lastName + '\'' +
                ", initials='" + initials + '\'' +
                ", residence='" + residence + '\'' +
                ", party='" + partyName + '\'' +
				", listNumber=" + listNumber +
				", candidateIdentifier=" + candidateIdentifier +
                '}';
    }
}
