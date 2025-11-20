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
import java.time.Instant;

/**
 * A demo service for demonstrating how an EML-XML parser can be used inside a backend application.<br/>
 * <br/>
 * <i><b>NOTE: </b>There are some TODO's and FIXME's present that need fixing!</i>
 */
@Service
public class DutchElectionService {
    private static class CacheEntry {
        private final Election election;
        private final Instant cachedAt;

        public CacheEntry(Election election, Instant cachedAt) {
            this.election = election;
            this.cachedAt = cachedAt;
        }

        public Election getElection() {
            return election;
        }

        public Instant getCachedAt() {
            return cachedAt;
        }

        public boolean isExpired(long hours) {
            return Instant.now().isAfter(cachedAt.plusSeconds(hours * 3600));
        }
    }

    private final Map<String, CacheEntry> electionCache = new HashMap<>();
    private static final long CACHE_EXPIRATION_HOURS = 1;

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
                    for (Party party : municipality.getAllParties()) { // ✅ now Party instead of PartyResult
                        if (election.getPartyById(party.getId()) == null) {
                            election.addParty(new Party(party.getId(), party.getName()));
                        }
                    }
                }
            }

            electionCache.put(electionId, new CacheEntry(election, Instant.now()));


            System.out.println("Dutch Election results: " + election);
            return election;
        } catch (IOException | XMLStreamException | NullPointerException | ParserConfigurationException |
                 SAXException e) {
            System.err.println("Failed to process the election results!");
            e.printStackTrace();
            return null;
        }
    }

    public Election getElectionById(String electionId) {
        CacheEntry entry = electionCache.get(electionId);
        if (entry == null) {
            return null;
        }

        if (entry.isExpired(CACHE_EXPIRATION_HOURS)) {
            
            electionCache.remove(electionId);
            return null;
        }
        
        return entry.getElection();
    }

    public void loadCandidateLists(Election election, String folderName) {
        String electionId = election.getId().trim();
        folderName = folderName.trim();

        // Check election already exists in cache
        CacheEntry cachedEntry = electionCache.get(electionId);
        if (cachedEntry != null && !cachedEntry.isExpired(CACHE_EXPIRATION_HOURS)) {
            Election cachedElection = cachedEntry.getElection();
            // candidates are already loaded, skip parsing 
            if (cachedElection != null && !cachedElection.getCandidates().isEmpty()) {
                System.out.println("Candidate lists already cached for election: " + electionId + 
                    " (candidates: " + cachedElection.getCandidates().size() + ") - using cache");
                return;
            }
        }

        System.out.println("Candidates not in cache - parsing candidate lists and total votes...");

        DutchElectionParser electionParser = new DutchElectionParser(
                new DutchDefinitionTransformer(election),
                new DutchCandidateTransformer(election),
                new DutchResultTransformer(election),
                new DutchNationalVotesTransformer(election),
                new DutchConstituencyVotesTransformer(election),
                new DutchMunicipalityVotesTransformer(election)
        );

        try {
            String safeFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
            System.out.println("Resolved folder name: " + safeFolderName);

            // Parse only candidate lists (from Kandidatenlijsten folder) and Totaaltelling
            // This will load both candidates and their vote counts without loading all other data
            System.out.println("Step 1: Loading candidate lists...");
            electionParser.parseCandidateListsAndTotalVotes(electionId,
                    PathUtils.getResourcePath("/" + safeFolderName));
            
            // Debug: Print summary of loaded candidates
            System.out.println("Step 2: Summary - Loaded " + election.getCandidates().size() + " candidates");
            long candidatesWithShortCode = election.getCandidates().stream()
                    .filter(c -> c.getShortCode() != null && !c.getShortCode().trim().isEmpty())
                    .count();
            System.out.println("  - Candidates with shortCode: " + candidatesWithShortCode);
            long candidatesWithVotes = election.getCandidates().stream()
                    .filter(c -> c.getVotes() > 0)
                    .count();
            System.out.println("  - Candidates with votes > 0: " + candidatesWithVotes);

            electionCache.put(electionId, new CacheEntry(election, Instant.now()));
            System.out.println("Candidate lists and total votes loaded for election: " + electionId);

        } catch (IOException | XMLStreamException | ParserConfigurationException |
                 SAXException | NullPointerException e) {
            System.err.println("Failed to load candidate lists!");
            e.printStackTrace();
        }
    }
}
