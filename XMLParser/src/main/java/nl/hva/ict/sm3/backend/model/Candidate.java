package nl.hva.ict.sm3.backend.model;

/**
 * Represents a candidate participating in an election.
 * <p>
 * A candidate is associated with a political party and contains personal information
 * such as name, initials, residence, and vote count. The candidate identifier
 * represents their position on the party's candidate list.
 */
public class Candidate {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final String initials;
    private final String residence;
    private final String partyId;
    private final String partyName;
	private final int candidateIdentifier;
    /** Short code used in votes files, typically lastName + initials (e.g., "YeşilgözD") */
    private String shortCode;
    /** The number of votes received by this candidate */
    private int votes;

	/**
     * Constructs a new Candidate with the specified information.
     * Initializes votes to 0 and shortCode to null.
     *
     * @param id the unique identifier for the candidate (typically partyId-candidateId)
     * @param firstName the candidate's first name
     * @param lastName the candidate's last name
     * @param initials the candidate's initials
     * @param residence the candidate's place of residence
     * @param partyId the unique identifier of the political party
     * @param partyName the name of the political party
     * @param candidateIdentifier the candidate's position on the party list
     */
	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int candidateIdentifier) {
        this(id, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier, null, 0);
    }

	/**
     * Constructs a new Candidate with the specified information and vote count.
     * Initializes shortCode to null.
     *
     * @param id the unique identifier for the candidate (typically partyId-candidateId)
     * @param firstName the candidate's first name
     * @param lastName the candidate's last name
     * @param initials the candidate's initials
     * @param residence the candidate's place of residence
     * @param partyId the unique identifier of the political party
     * @param partyName the name of the political party
     * @param candidateIdentifier the candidate's position on the party list
     * @param votes the initial number of votes for this candidate
     */
	public Candidate(String id, String firstName, String lastName, String initials, String residence, String partyId, String partyName, int candidateIdentifier, int votes) {
        this(id, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier, null, votes);
    }

	/**
     * Constructs a new Candidate with all specified information.
     *
     * @param id the unique identifier for the candidate (typically partyId-candidateId)
     * @param firstName the candidate's first name
     * @param lastName the candidate's last name
     * @param initials the candidate's initials
     * @param residence the candidate's place of residence
     * @param partyId the unique identifier of the political party
     * @param partyName the name of the political party
     * @param candidateIdentifier the candidate's position on the party list
     * @param shortCode the short code used in votes files (e.g., "YeşilgözD")
     * @param votes the initial number of votes for this candidate
     */
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

    /**
     * Returns the unique identifier of this candidate.
     *
     * @return the candidate's unique ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the first name of this candidate.
     *
     * @return the candidate's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name of this candidate.
     *
     * @return the candidate's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the initials of this candidate.
     *
     * @return the candidate's initials
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Returns the place of residence of this candidate.
     *
     * @return the candidate's residence
     */
    public String getResidence() {
        return residence;
    }

    /**
     * Returns the unique identifier of the political party this candidate belongs to.
     *
     * @return the party's unique ID
     */
    public String getPartyId() {
        return partyId;
    }

    /**
     * Returns the name of the political party this candidate belongs to.
     *
     * @return the party's name
     */
    public String getPartyName() {
        return partyName;
    }

	/**
     * Returns the candidate identifier, which represents their position on the party's candidate list.
     *
     * @return the candidate's position identifier
     */
	public int getCandidateIdentifier() {
		return candidateIdentifier;
	}

    /**
     * Returns the short code used in votes files.
     * Typically formatted as lastName + initials (e.g., "YeşilgözD").
     *
     * @return the candidate's short code, or null if not set
     */
    public String getShortCode() {
        return shortCode;
    }

    /**
     * Sets the short code for this candidate.
     * This is typically used when processing votes files.
     *
     * @param shortCode the short code to set (e.g., "YeşilgözD")
     */
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    /**
     * Returns the number of votes received by this candidate.
     *
     * @return the vote count
     */
    public int getVotes() {
        return votes;
    }

    /**
     * Sets the number of votes for this candidate.
     *
     * @param votes the new vote count
     */
    public void setVotes(int votes) {
        this.votes = votes;
    }

    /**
     * Adds votes to this candidate's current vote count.
     *
     * @param votes the number of votes to add
     */
    public void addVotes(int votes) {
        this.votes += votes;
    }

    /**
     * Returns a string representation of this candidate.
     * Includes ID, name, initials, residence, party, candidate identifier, and vote count.
     *
     * @return a string representation of the candidate
     */
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
