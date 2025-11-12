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
        String shortCode = electionData.getOrDefault(CANDIDATE_IDENTIFIER_SHORT_CODE, null); // no

        // Votes etc
        int validVotes = parseIntSafe(electionData.getOrDefault(VALID_VOTES, "0"));
        int rejectedVotes = parseIntSafe(electionData.getOrDefault(REJECTED_VOTES, "0")); // no
        int totalCounted = parseIntSafe(electionData.getOrDefault(TOTAL_COUNTED, "0")); // no
        int numberOfSeats = parseIntSafe(electionData.getOrDefault(NUMBER_OF_SEATS, "0")); // no
        /*
        int numberOfSeats;
        try {
            numberOfSeats = Integer.parseInt(numberOfSeats);
        } catch (NumberFormatException e) {
            numberOfSeats = 0;
        }
        */

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


