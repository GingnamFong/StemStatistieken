package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;
import nl.hva.ict.sm3.backend.model.National;

import java.util.Map;


/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */

public class DutchNationalVotesTransformer implements VotesTransformer, TagAndAttributeNames {
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

        // Core
        String electionId = electionData.getOrDefault(ELECTION_IDENTIFIER, "unknown"); // no
        String electionName = electionData.getOrDefault(ELECTION_NAME, "Unknown Election");

        // Party info
        String partyId = electionData.getOrDefault(AFFILIATION_IDENTIFIER + "-Id", "unknown");
        String partyName = electionData.getOrDefault(REGISTERED_NAME, "Unknown Party");
        String shortCode = electionData.getOrDefault(CANDIDATE_IDENTIFIER_SHORT_CODE, null);

        // Votes etc
        int validVotes = parseIntSafe(electionData.getOrDefault(VALID_VOTES, "0"));
        int rejectedVotes = parseIntSafe(electionData.getOrDefault(REJECTED_VOTES, "0")); // no
        int totalCounted = parseIntSafe(electionData.getOrDefault(TOTAL_COUNTED, "0")); // no
        int numberOfSeats = parseIntSafe(electionData.getOrDefault(NUMBER_OF_SEATS, "0")); // no

        //unique id
        String uniqueId = String.format("%s-%s", electionId, partyId);

        National result = new National(
                uniqueId,
                electionId,
                electionName,
                partyId,
                partyName,
                shortCode,
                validVotes,
                rejectedVotes,
                totalCounted,
                numberOfSeats
        );

        System.out.println("Registering national result: " + result);

        boolean alreadyExists = election.getNationalVotes().stream()
                .anyMatch(r -> r.getId().equals(uniqueId));

        if (!alreadyExists) {
            election.addNationalVotes(result);
            System.out.println("Registered national result: " + result);
        } else {
            System.out.println("Skipped duplicate: " + result);
        }
    }

    // Helper to safely parse integers
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

        /*
            String Id = electionData.getOrDefault("Id", "unknown");
            String votingMethod = electionData.getOrDefault("VotingMethod", "unknown");
            int maxVotes = parseIntSafe(electionData.get("MaxVotes"));
            int uncountedVotes = parseIntSafe(electionData.get("UncountedVotes"));
            int validVotes = parseIntSafe(electionData.get("ValidVotes"));
            int totalVotes = parseIntSafe(electionData.get("TotalVotes"));

            String ID = "Id";
            String MAX_VOTES = "MaxVotes";
            String UNCOUNTED_VOTES = "UncountedVotes";
            String VALID_VOTES = "ValidVotes";
            String VOTING_METHOD = "VotingMethod";
            String TOTAL_VOTES = "TotalVotes";

                ELECTION_IDENTIFIER,
                ELECTION_NAME,
                CONTEST_IDENTIFIER,
                NUMBER_OF_SEATS,
                REGISTERED_PARTY,
                REGISTERED_NAME,
                SHORT_CODE,
                VALID_VOTES,
                REJECTED_VOTES,
                TOTAL_COUNTED,
                RESULT


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

        System.out.printf("%s party votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    private int parseIntSafe(String maxVotes) {
        return 0;
    }
    */

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s candidate votes: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        System.out.printf("%s meta data: %s\n", aggregated ? "National" : "Constituency", electionData);
    }

    //comment
}


