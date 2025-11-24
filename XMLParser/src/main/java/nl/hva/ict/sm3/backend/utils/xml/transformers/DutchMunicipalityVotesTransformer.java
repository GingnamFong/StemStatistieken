package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchMunicipalityVotesTransformer implements VotesTransformer {
    /** The election instance to which data will be added. */
    private final Election election;

    /**
     * Creates a new transformer for registering municipality vote data
     * into the given election model.
     *
     * @param election the election object where parsed data will be stored
     */
    public DutchMunicipalityVotesTransformer(Election election) {
        this.election = election;
    }

    /**
     * Registers aggregated party-level votes for a municipality.
     * <p>
     * The method extracts municipality identifiers, party information, and
     * vote counts from the provided {@code electionData} map and inserts them
     * into the correct {@link Constituency} and {@link Municipality} within
     * the {@link Election}. If a municipality does not yet exist, it is created.
     * </p>
     *
     * @param aggregated   whether this entry contains aggregated vote data
     * @param electionData a map of XML key-value pairs representing election data
     */
    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> data) {
        // Municipality-level aggregated votes
        if (aggregated) {
            handleMunicipalityVotes(data);
        }
    }

    // ---------------------------------------------------------
    // MUNICIPALITY (aggregated = true)
    // ---------------------------------------------------------
    private void handleMunicipalityVotes(Map<String, String> data) {
        String municipalityName = data.getOrDefault("AuthorityIdentifier", "unknown");
        String municipalityId = data.getOrDefault("AuthorityIdentifier-Id", "unknown");
        String constituencyId = data.getOrDefault("ContestIdentifier-Id", "unknown");

        String partyId = data.getOrDefault("AffiliationIdentifier-Id", "unknown");
        String partyName = data.getOrDefault("RegisteredName", "unknown");
        int validVotes = Integer.parseInt(data.getOrDefault("ValidVotes", "0"));

        Constituency constituency = election.getConstituencyById(constituencyId);
        if (constituency == null) return;

        Municipality municipality = constituency.getMunicipalityById(municipalityId);
        if (municipality == null) {
            municipality = new Municipality(municipalityId, municipalityName, 0);
            constituency.addMunicipality(municipality);
        }

        municipality.addVotesForParty(partyId, partyName, validVotes);
    }


    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Not needed here
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Not needed here
    }
}
