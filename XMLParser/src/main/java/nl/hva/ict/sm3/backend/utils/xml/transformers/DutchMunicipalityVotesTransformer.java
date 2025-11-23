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
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        if (aggregated) {
            // Extract municipality info
            String municipalityName = electionData.getOrDefault("AuthorityIdentifier", "unknown");
            String contestId = electionData.getOrDefault("ContestIdentifier-Id", "unknown");
            String municipalityId = electionData.getOrDefault("AuthorityIdentifier-Id", "unknown");
            String validVotesStr = electionData.getOrDefault("ValidVotes", "0");
            String partyId = electionData.getOrDefault("AffiliationIdentifier-Id", "unknown");
            String partyName = electionData.getOrDefault("RegisteredName", "unknown");

            int validVotes = 0;

            try {
                validVotes = Integer.parseInt(validVotesStr);
            } catch (NumberFormatException e) {
                System.err.printf("⚠️ Invalid number for ValidVotes '%s' in municipality '%s'%n", validVotesStr, municipalityName);
            }


            // Find the constituency
            Constituency constituency = election.getConstituencyById(contestId);


            if (constituency != null) {
                Municipality municipality = constituency.getMunicipalityById(municipalityId);
                if (municipality == null) {
                    municipality = new Municipality(municipalityId, municipalityName, validVotes);
                    constituency.addMunicipality(municipality);
                }
                // add votes per party
                municipality.addVotesForParty(partyId,partyName,validVotes);

                System.out.printf("Added municipality: %s with %d votes to constituency %s%n",
                        municipalityName, validVotes, constituency.getName());
                System.out.printf("Municipality: %s, ContestId: %s\n", municipalityName, contestId);
            } else {
                System.err.printf("Warning: constituency with id '%s' not found for municipality '%s'%n",
                        contestId, municipalityName);
            }
        }
    }


    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Implement candidate-level votes if needed
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Implement metadata handling if needed
    }
}
