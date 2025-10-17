package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.CandidateTransformer;

import java.util.Map;

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

        String candidateId = electionData.getOrDefault("CandidateIdentifier-Id", "unknown");
        String initials = electionData.getOrDefault("NameLine", "unknown");
        String firstName = electionData.getOrDefault("FirstName", "unknown");
        String lastName = electionData.getOrDefault("LastName", "unknown");
        String residence = electionData.getOrDefault("LocalityName", "unknown");
        String partyId = electionData.getOrDefault("AffiliationIdentifier-Id", "unknown");
        String partyName = electionData.getOrDefault("RegisteredName", "Unknown Party");

        Candidate candidate = new Candidate(candidateId, firstName, lastName, initials, residence, partyId, partyName);

        System.out.println("Registering candidate: " + electionData);
    }
}
