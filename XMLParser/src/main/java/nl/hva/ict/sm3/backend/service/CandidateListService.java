package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.PathUtils;
import nl.hva.ict.sm3.backend.utils.xml.DutchElectionParser;
import nl.hva.ict.sm3.backend.utils.xml.transformers.*;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for loading candidate lists from election data files.
 * Handles parsing of candidate lists and total votes without loading all election data.
 */
@Service
public class CandidateListService {
    private final DutchElectionService electionService;

    public CandidateListService(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    /**
     * Loads candidate lists and total votes for the given election.
     * Checks cache first to avoid redundant parsing.
     *
     * @param election the election to load candidate lists for
     * @param folderName the folder name containing the election data files
     */
    public void loadCandidateLists(Election election, String folderName) {
        String electionId = election.getId().trim();
        folderName = folderName.trim();


        // Check election already exists in cache
        Election cachedElection = electionService.getElectionById(electionId);
        if (cachedElection != null && !cachedElection.getCandidates().isEmpty()) {
            // Copy candidates from cached election to the provided election object
            cachedElection.getCandidates().stream()
                    .filter(c -> election.getCandidates().stream().noneMatch(e -> e.getId().equals(c.getId())))
                    .forEach(election::addCandidate);
            return;
        }

        DutchElectionParser electionParser = new DutchElectionParser(
                new DutchDefinitionTransformer(election),
                new DutchCandidateTransformer(election),
                new DutchResultTransformer(election),
                new DutchNationalVotesTransformer(election),
                new DutchConstituencyVotesTransformer(election),
                new DutchMunicipalityVotesTransformer(election),
                new DutchPollingStationVotesTransformer(election)
        );

        try {
            String safeFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
            electionParser.parseCandidateListsAndTotalVotes(electionId,
                    PathUtils.getResourcePath("/" + safeFolderName));
            
            electionService.cacheElection(electionId, election);
        } catch (IOException | XMLStreamException | ParserConfigurationException |
                 SAXException | NullPointerException e) {
            System.err.println("Failed to load candidate lists: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

