package nl.hva.ict.sm3.backend.dto;

/**
 * DTO for stemwijzer functionality.
 * Used for both match results (response) and favorite party requests.
 * For match results: all fields are populated.
 * For favorite party requests: only partyId is required.
 */
public class StemwijzerDto {
    private String partyId;
    private String partyName;
    private String partyColor;
    private Integer matchPercentage;

    // Default constructor
    public StemwijzerDto() {
    }

    // Constructor with all fields
    public StemwijzerDto(String partyId, String partyName, String partyColor, Integer matchPercentage) {
        this.partyId = partyId;
        this.partyName = partyName;
        this.partyColor = partyColor;
        this.matchPercentage = matchPercentage;
    }

    // Getters and setters
    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getPartyColor() {
        return partyColor;
    }

    public void setPartyColor(String partyColor) {
        this.partyColor = partyColor;
    }

    public Integer getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(Integer matchPercentage) {
        this.matchPercentage = matchPercentage;
    }
}
