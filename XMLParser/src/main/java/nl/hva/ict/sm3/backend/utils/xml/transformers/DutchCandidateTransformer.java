package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.CandidateTransformer;
import nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames;

import java.util.Map;

import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.*;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchCandidateTransformer implements CandidateTransformer {
    private final Election election;

    /**
     * Creates a new transformer for handling the candidate lists. It expects an instance of Election that can
     * be used for storing the candidates lists.
     * @param election the election in which the candidate lists wil be stored.
     */
    public DutchCandidateTransformer(Election election) {
        this.election = election;
    }

    @Override
    public void registerCandidate(Map<String, String> electionData) {

        String candidateId = electionData.getOrDefault(CANDIDATE_IDENTIFIER_ID, "unknown");
        String initials = electionData.getOrDefault(NAME_LINE, "unknown");
        String firstName = electionData.getOrDefault(FIRST_NAME, "unknown");
        String lastName = electionData.getOrDefault(LAST_NAME, "unknown");
        String residence = electionData.getOrDefault(LOCALITY_NAME, "unknown");
        String partyId = electionData.getOrDefault(AFFILIATION_IDENTIFIER + "-Id", "unknown");
        String partyName = electionData.getOrDefault(REGISTERED_NAME, "Unknown Party");
		int candidateIdentifier;
		try {
			candidateIdentifier = Integer.parseInt(candidateId);
		} catch (NumberFormatException e) {
			candidateIdentifier = 0;
		}
        // Ensure uniqueness across parties: ranking numbers repeat per party, so compose a unique id
        String uniqueId = String.format("%s-%s", partyId, candidateId);
		Candidate candidate = new Candidate(uniqueId, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier);

        System.out.println("Registering candidate: " + candidate);

        boolean alreadyExists = election.getCandidates().stream()
                .anyMatch(c -> c.getId().equals(uniqueId));

        if (!alreadyExists) {
            election.addCandidate(candidate);
            System.out.println("Registered candidate: " + candidate);
        } else {
            System.out.println("Skipped duplicate: " + candidate);
        }
    }
}
