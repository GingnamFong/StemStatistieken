package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Party;
import nl.hva.ict.sm3.backend.utils.xml.DefinitionTransformer;
import nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames;

import java.util.Map;

/**
 * Transforms definition data (regions, parties) from XML into the Election model.
 */
public class DutchDefinitionTransformer implements DefinitionTransformer, TagAndAttributeNames {
    private final Election election;

    /**
     * Creates a new transformer for handling the structure of the constituencies, municipalities and the parties.
     * It expects an instance of Election that can be used for storing the results.
     * @param election the election in which the votes wil be stored.
     */
    public DutchDefinitionTransformer(Election election) {
        this.election = election;
    }
    
    @Override
    public void registerRegion(Map<String, String> electionData) {
        System.out.println("Committee: " + electionData);
    }

    @Override
    public void registerParty(Map<String, String> electionData) {
        // Extract party ID and name from the election data
        String partyId = electionData.getOrDefault(AFFILIATION_IDENTIFIER + "-Id", null);
        String partyName = electionData.getOrDefault(REGISTERED_NAME, null);
        
        // Only add party if we have both ID and name
        if (partyId != null && partyName != null && !partyId.isEmpty() && !partyName.isEmpty()) {
            // Check if party already exists to avoid duplicates
            if (election.getPartyById(partyId) == null) {
                Party party = new Party(partyId, partyName);
                election.addParty(party);
                System.out.println("Registered party: " + partyId + " - " + partyName);
            }
        } else {
            System.out.println("Skipping party registration - missing ID or name. Data: " + electionData);
        }
    }
}
