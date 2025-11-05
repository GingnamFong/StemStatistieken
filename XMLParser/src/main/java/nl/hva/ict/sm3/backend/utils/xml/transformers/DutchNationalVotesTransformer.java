package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;

import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.*;

/**
 * Handles votes at the national level and stores candidate votes in the election model.
 */
public class DutchNationalVotesTransformer implements VotesTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the votes at the national level. It expects an instance of
     * Election that can be used for storing the results.
     * @param election the election in which the votes wil be stored.
     */
    public DutchNationalVotesTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s party votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Only process aggregated (national) votes
        if (aggregated) {
            String shortCode = electionData.get(CANDIDATE_IDENTIFIER_SHORT_CODE);
            String validVotesStr = electionData.getOrDefault(VALID_VOTES, "0");
            
            if (shortCode != null && !shortCode.trim().isEmpty()) {
                try {
                    int votes = Integer.parseInt(validVotesStr.trim());
                    Candidate candidate = election.getCandidateByShortCode(shortCode.trim());
                    
                    if (candidate != null) {
                        candidate.addVotes(votes);
                        System.out.printf("Added %d votes to candidate %s (ShortCode: %s)\n", 
                                votes, candidate.getLastName(), shortCode);
                    } else {
                        System.out.printf("Warning: Candidate with ShortCode '%s' not found in candidate list\n", shortCode);
                    }
                } catch (NumberFormatException e) {
                    System.err.printf("Invalid vote count '%s' for candidate with ShortCode '%s'\n", 
                            validVotesStr, shortCode);
                }
            } else {
                System.out.printf("National candidate votes (no ShortCode): %s\n", electionData);
            }
        }
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s meta data: %s\n", aggregated ? "National" : "Constituency", electionData);
    }
}

///  comment test
