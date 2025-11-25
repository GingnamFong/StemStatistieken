package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.CandidateTransformer;

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

    /**
     * Registers a candidate from parsed election data.
     * <p>
     * Extracts candidate information from the provided election data map and creates
     * a new Candidate instance. The candidate is added to the election if it doesn't
     * already exist (based on unique ID). Duplicate candidates are skipped.
     * <p>
     * The unique ID is composed as "partyId-candidateId" to ensure uniqueness
     * across different parties, as ranking numbers repeat per party.
     *
     * @param electionData a map containing candidate data extracted from XML tags,
     *                     including candidate ID, name, initials, residence, party information, etc.
     */
    @Override
    public void registerCandidate(Map<String, String> electionData) {

        String candidateId = electionData.getOrDefault(CANDIDATE_IDENTIFIER_ID, "unknown");
        // Note: shortCode is not present in kandidatenlijsten files, only in votes files
        String shortCode = electionData.getOrDefault(CANDIDATE_IDENTIFIER_SHORT_CODE, null);
        
        // Prefer NameLine with NameType="Initials", fall back to plain NameLine
        String initials = electionData.getOrDefault(String.format("%s-%s", NAME_LINE, "Initials"), 
                                                     electionData.getOrDefault(NAME_LINE, "unknown"));
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
		Candidate candidate = new Candidate(uniqueId, firstName, lastName, initials, residence, partyId, partyName, candidateIdentifier, shortCode, 0);

        boolean alreadyExists = election.getCandidates().stream()
                .anyMatch(c -> c.getId().equals(uniqueId));

        if (!alreadyExists) {
            election.addCandidate(candidate);
            System.out.println("Registered candidate: " + candidate.getFirstName() + " " + candidate.getLastName() +
                " (ID: " + candidateId + ", Party: " + partyName + ")");
        } else {
            System.out.println("Skipped duplicate: " + candidate);
        }
    }
}
