package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;
import nl.hva.ict.sm3.backend.dto.NationalDto;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


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
        // ElectionInfo electionInfo = new ElectionInfo(electionId, electionName);

        // Party info
        String partyId = electionData.getOrDefault(AFFILIATION_IDENTIFIER + "-Id", "unknown");
        String partyName = electionData.getOrDefault(REGISTERED_NAME, "Unknown Party");
        String shortCode = electionData.getOrDefault(CANDIDATE_IDENTIFIER_SHORT_CODE, null); // no
       //  PartyInfo partyInfo = new PartyInfo(partyId, partyName, shortCode);

        // Look for the seats data
        int validVotes = parseIntSafe(electionData.getOrDefault(VALID_VOTES, "0"));
        int numberOfSeats = parseIntSafe(electionData.getOrDefault(NUMBER_OF_SEATS, "0")); // no
        // VoteTotals voteTotals = new VoteTotals(validVotes, numberOfSeats);

        // Seperated from the rest of the data up here
        int rejectedVotes = parseIntSafe(electionData.getOrDefault(REJECTED_VOTES, "0")); // no
        int totalCounted = parseIntSafe(electionData.getOrDefault(TOTAL_COUNTED, "0")); // no
        // RejectedData rejectedData = new RejectedData(rejectedVotes, totalCounted);

        String combinedId = String.format("%s-%s-PARTY_VOTES", electionId, partyId);

        National combinedRecord = National.forCombined(
                combinedId,
                electionId,
                electionName,
                partyId,
                partyName,
                shortCode,
                validVotes,
                rejectedVotes,
                totalCounted,
                numberOfSeats,
                null // type if you have one; otherwise pass null
        );

        addOrReplace(combinedRecord);
    }

    /**
     * Replace the existing record with the same id or add if missing.
     * We search the Election's national votes list for a matching id and replace it.
     */
    private void addOrReplace(National record) {
        // Use the Election's replace method if it exists, otherwise find and replace manually
        List<National> nationals = election.getNationalVotes();

        int existingIndex = -1;
        for (int i = 0; i < nationals.size(); i++) {
            National r = nationals.get(i);
            if (r.getId().equals(record.getId())) {
                existingIndex = i;
                break;
            }
        }

        if (existingIndex >= 0) {
            // Use the Election's replace method
            election.replaceNationalVote(record.getId(), record);
            System.out.println("Replaced national result: " + record);
        } else {
            election.addNationalVotes(record);
            System.out.println("Added national result: " + record);
        }
    }

/*
        String baseId = String.format("%s-%s", electionId, partyId);

        // PARTY_VOTES record
        National votesRecord = National.forPartyVotes(baseId + "-PARTY_VOTES",
                electionId, electionName, partyId, partyName, shortCode, validVotes);
        addIfNotExists(votesRecord);

        // REJECTED_DATA record
        National rejectedRecord = National.forRejectedData(baseId + "-REJECTED",
                electionId, electionName, partyId, partyName, shortCode, rejectedVotes, totalCounted);
        addIfNotExists(rejectedRecord);

        // SEATS record
        National seatsRecord = National.forSeats(baseId + "-SEATS",
                electionId, electionName, partyId, partyName, shortCode, numberOfSeats);
        addIfNotExists(seatsRecord);
    }

    private void addIfNotExists(National record) {
        boolean alreadyExists = election.getNationalVotes().stream()
                .anyMatch(r -> r.getId().equals(record.getId()) && r.getType() == record.getType());

        if (!alreadyExists) {
            election.addNationalVotes(record);
            System.out.println("Registered national result: " + record);
        } else {
            System.out.println("Skipped duplicate: " + record);
        }
    }

        /*
        National result = new National(
                uniqueId,
                electionInfo,
                partyInfo,
                voteTotals,
                rejectedData
        );
         */

    /*

        //unique id
        String uniqueId = String.format("%s-%s", electionId, partyId);

        National result = new National(
                uniqueId,

                //election
                electionId,
                electionName,

                //party
                partyId,
                partyName,
                shortCode,

                // votes
                validVotes, // wachten op oplossing, zoeken welk xml bestand deze data zit
                rejectedVotes, // apart

                // counted rejected
                totalCounted, // apart
                numberOfSeats // wachten op oplossing, zoeken welk xml bestand deze data zit
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

     */

    // Helper to safely parse integers
    private int parseIntSafe(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static final class SeatAllocator {
        private SeatAllocator() {}
        public static Map<String, Integer> allocateDHondt(Map<String, Long> votes, int totalSeats) {
            Map<String, Integer> seats = new HashMap<>();
            for (String party : votes.keySet()) seats.put(party, 0);

            for (int i = 0; i < totalSeats; i++) {
                String bestParty = null;
                double bestQuotient = -1.0;
                for (Map.Entry<String, Long> e : votes.entrySet()) {
                    String party = e.getKey();
                    long partyVotes = e.getValue() == null ? 0L : e.getValue();
                    int already = seats.getOrDefault(party, 0);
                    double quotient = partyVotes / (double) (already + 1);
                    if (quotient > bestQuotient) {
                        bestQuotient = quotient;
                        bestParty = party;
                    } else if (quotient == bestQuotient) {
                        long currentBestVotes = votes.getOrDefault(bestParty, 0L);
                        if (partyVotes > currentBestVotes) bestParty = party;
                        else if (partyVotes == currentBestVotes && party.compareTo(bestParty) < 0) bestParty = party;
                    }
                }
                seats.put(bestParty, seats.getOrDefault(bestParty, 0) + 1);
            }
            return seats;
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


}

        /*
        int numberOfSeats;
        try {
            numberOfSeats = Integer.parseInt(numberOfSeats);
        } catch (NumberFormatException e) {
            numberOfSeats = 0;
        }
        */
