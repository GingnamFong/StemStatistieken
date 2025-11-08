package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;
import nl.hva.ict.sm3.backend.model.National;

import java.util.Map;


/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
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
        if (aggregated) {

            String Id = electionData.get("id");
            String votingMethod = electionData.getOrDefault("voting_method", "unknown");
            int maxVotes = Integer.parseInt(electionData.getOrDefault("max_votes", "0"));
            int uncountedVotes = Integer.parseInt(electionData.getOrDefault("uncounted_votes", "0"));
            int validVotes = Integer.parseInt(electionData.getOrDefault("valid_votes", "0"));
            int totalVotes = Integer.parseInt(electionData.getOrDefault("total_votes", "0"));

            /*
            String ID = "Id";
            String MAX_VOTES = "MaxVotes";
            String UNCOUNTED_VOTES = "UncountedVotes";
            String VALID_VOTES = "ValidVotes";
            String VOTING_METHOD = "VotingMethod";
            String TOTAL_VOTES = "TotalVotes";
             */

            National national = new National(Id, votingMethod,  maxVotes, uncountedVotes, validVotes, totalVotes);

            System.out.println("Registering national vote data: " + national);

            boolean alreadyExist = election.getNationalVotes().stream()
                    .anyMatch(c -> c.getId().equals(national.getId()));

            if (!alreadyExist) {
                election.addNationalVotes(national);
                System.out.println("Added national votes for: " + national);
            } else {
                System.out.println("Already exists national votes for: " + national);
            }

            try {
                maxVotes = Math.max(maxVotes, maxVotes);
            } catch (Exception e) {
                System.err.printf("maxVotes = %d\n", maxVotes);
            }


        }
        System.out.printf("%s party votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s candidate votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s meta data: %s\n", aggregated ? "National" : "Constituency", electionData);
    }
}


