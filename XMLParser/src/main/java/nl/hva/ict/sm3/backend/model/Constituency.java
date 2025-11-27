package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents a Dutch *Constituency* ("Kieskring") within an election.
 *
 * <p>A constituency contains one or more municipalities and maintains
 * the total number of valid votes from all municipalities combined.
 * Constituencies are part of the hierarchical election structure:
 *
 * <pre>
 * Election → Constituency → Municipality → PollingStation
 * </pre>
 *
 * <p>This class stores the constituency metadata (ID and name) along with
 * its municipalities and aggregated vote totals.</p>
 */
public class Constituency {
    private String id;
    private String name;
    private List<Municipality> municipalities = new ArrayList<>();
    private int totalVotes = 0;

    /**
     * Creates a new Constituency instance.
     *
     * @param id   unique identifier for the constituency
     * @param name display name of the constituency
     */
    public Constituency(String id, String name) {
        this.id = id;
        this.name = name;
    }
    /**
     * Adds a municipality to this constituency and automatically
     * increases the total valid vote count.
     *
     * <p><b>Business rule:</b> Whenever a municipality is added,
     * all its valid votes must be included in the constituency total.</p>
     *
     * @param m the municipality to add
     */

    public void addMunicipality(Municipality m) {
        municipalities.add(m);
        totalVotes += m.getValidVotes(); // sum votes
    }
    /**
     * Searches for a municipality within this constituency by ID.
     *
     * @param municipalityId the municipality ID to search for
     * @return the matching municipality, or {@code null} if not found
     */
    public Municipality getMunicipalityById(String municipalityId) {
        for (Municipality m : municipalities) {
            if (m.getId().equals(municipalityId)) {
                return m;
            }
        }
        return null;
    }
    /**
     * Adds votes manually to the total vote count.
     * Used during result parsing when votes are aggregated step-by-step.
     *
     * @param votes number of additional valid votes
     */
    public void addToTotalVotes(int votes) {
        this.totalVotes += votes;
    }
    /**
     * @return constituency ID
     */
    public String getId() {
        return id;
    }
    /**
     * @return constituency name
     */
    public String getName() {
        return name;
    }
    /**
     * @return list of municipalities belonging to this constituency
     */
    public List<Municipality> getMunicipalities() { return municipalities; }

    /**
     * Provides a debug-friendly representation of the constituency,
     * including its ID, name, total votes, and municipalities.
     */
    @Override
    public String toString() {
        return "Constituency{id='%s', name='%s', totalVotes=%d, municipalities=%s}".formatted(
                id, name, totalVotes, municipalities
        );
    }
}
