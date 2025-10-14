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
    private final Election election;

    public DutchMunicipalityVotesTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        if (aggregated) {
            // Extract municipality info
            String municipalityName = electionData.getOrDefault("AuthorityIdentifier", "unknown");
            String contestId = electionData.getOrDefault("ContestIdentifier-Id", "unknown");
            String municipalityId = electionData.getOrDefault("AuthorityIdentifier-Id", "unknown");

            String validVotesStr = electionData.getOrDefault("ValidVotes", "0");
            int validVotes = 0;
            try {
                validVotes = Integer.parseInt(validVotesStr);
            } catch (NumberFormatException e) {
                System.err.printf("⚠️ Invalid number for ValidVotes '%s' in municipality '%s'%n", validVotesStr, municipalityName);
            }


            // Find the constituency
            Constituency constituency = election.getConstituencyById(contestId);


            if (constituency != null) {
                // Add the municipality — totalVotes is updated automatically
                constituency.addMunicipality(new Municipality(municipalityName, validVotes));
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
