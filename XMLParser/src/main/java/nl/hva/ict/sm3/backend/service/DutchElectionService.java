package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.utils.PathUtils;
import nl.hva.ict.sm3.backend.utils.xml.*;
import nl.hva.ict.sm3.backend.utils.xml.transformers.*;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A demo service for demonstrating how an EML-XML parser can be used inside a backend application.<br/>
 * <br/>
 * <i><b>NOTE: </b>There are some TODO's and FIXME's present that need fixing!</i>
 */
@Service
public class DutchElectionService {
    private final Map<String, Election> electionCache = new HashMap<>();

    public Election readResults(String electionId, String folderName) {
        System.out.println("Processing files...");


        electionId = electionId.trim();
        folderName = folderName.trim();

        Election election = new Election(electionId);
        // TODO This lengthy construction of the parser should be replaced with a fitting design pattern!
        //  And refactoring the constructor while your at it is also a good idea.
        DutchElectionParser electionParser = new DutchElectionParser(
                new DutchDefinitionTransformer(election),
                new DutchCandidateTransformer(election),
                new DutchResultTransformer(election),
                new DutchNationalVotesTransformer(election),
                new DutchConstituencyVotesTransformer(election),
                new DutchMunicipalityVotesTransformer(election)
        );

        try {
            // Clean and encode the folder name to prevent URI errors
            String safeFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
            System.out.println("Resolved folder name: " + safeFolderName);
            // Assuming the election data is somewhere on the class-path it should be found.
            electionParser.parseResults(electionId, PathUtils.getResourcePath("/" + safeFolderName));
            for (Constituency constituency : election.getConstituencies()) {
                for (Municipality municipality : constituency.getMunicipalities()) {
                    for (Map<String, Object> top : municipality.getTopPartiesWithNames(10)) { // 10 om alles te krijgen
                        String partyId = (String) top.get("id");
                        String partyName = (String) top.get("name");

                        if (election.getPartyById(partyId) == null) {
                            election.addParty(new Party(partyId, partyName)); // of gebruik echte naam uit XML
                        }

                        // stemmen per gemeente zijn al in Municipality.addVotes gezet door de transformer
                    }
                }
            }

            electionCache.put(electionId, election);


            System.out.println("Dutch Election results: " + election);
            return election;
        } catch (IOException | XMLStreamException | NullPointerException | ParserConfigurationException | SAXException e) {
            System.err.println("Failed to process the election results!");
            e.printStackTrace();
            return null;
        }
    }

    public Election getElectionById(String electionId) {
        return electionCache.get(electionId);
    }
}
