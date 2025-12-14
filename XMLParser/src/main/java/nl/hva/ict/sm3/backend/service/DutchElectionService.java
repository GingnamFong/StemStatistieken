package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.repository.*;
import nl.hva.ict.sm3.backend.utils.PathUtils;
import nl.hva.ict.sm3.backend.utils.xml.*;
import nl.hva.ict.sm3.backend.utils.xml.transformers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for loading Dutch election data from XML files and persisting to database.
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

        public boolean isExpired(long hours) {
            return Instant.now().isAfter(cachedAt.plusSeconds(hours * 3600));
        }
    }

    private final Map<String, CacheEntry> electionCache = new HashMap<>();
    private static final long CACHE_EXPIRATION_HOURS = 24; // Cache for 24 hours
    
    private final ElectionRepository electionRepository;
    private final PartyRepository partyRepository;
    private final CandidateRepository candidateRepository;
    private final NationalRepository nationalRepository;
    private final ConstituencyRepository constituencyRepository;
    private final MunicipalityRepository municipalityRepository;
    private final PollingStationRepository pollingStationRepository;
    
    @Autowired
    public DutchElectionService(ElectionRepository electionRepository,
                               PartyRepository partyRepository,
                               CandidateRepository candidateRepository,
                               NationalRepository nationalRepository,
                               ConstituencyRepository constituencyRepository,
                               MunicipalityRepository municipalityRepository,
                               PollingStationRepository pollingStationRepository) {
        this.electionRepository = electionRepository;
        this.partyRepository = partyRepository;
        this.candidateRepository = candidateRepository;
        this.nationalRepository = nationalRepository;
        this.constituencyRepository = constituencyRepository;
        this.municipalityRepository = municipalityRepository;
        this.pollingStationRepository = pollingStationRepository;
    }

    /**
     * Reads election results from XML files and saves to database.
     * If the election already exists in the database, returns it from there.
     */
    @Transactional
    public Election readResults(String electionId, String folderName) {
        System.out.println("=== readResults called for: " + electionId + " ===");

        electionId = electionId.trim();
        folderName = folderName.trim();

        // Check if election already exists in database AND has data
        // Use direct count query to avoid lazy loading issues
        if (electionRepository.existsById(electionId)) {
            long constituencyCount = constituencyRepository.countByElectionIdPrefix(electionId + "-");
            if (constituencyCount > 0) {
                System.out.println("Election " + electionId + " already exists with " + constituencyCount + " constituencies. Loading from DB...");
                return getElectionById(electionId);
            }
            // Election exists but is incomplete - delete and reload
            System.out.println("Election " + electionId + " exists but has 0 constituencies. Reloading...");
            electionRepository.deleteById(electionId);
            electionRepository.flush();
        }

        System.out.println("Election not in database, parsing XML files...");
        Election election = new Election(electionId);
        
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
            // Clean and encode the folder name to prevent URI errors
            String safeFolderName = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
            String resourcePath = PathUtils.getResourcePath("/" + safeFolderName);
            
            if (resourcePath == null) {
                System.err.println("ERROR: Could not find resource path for folder: " + folderName);
                return null;
            }
            
            System.out.println("Parsing from path: " + resourcePath);
            
            // Parse all election results from XML
            electionParser.parseResults(electionId, resourcePath);
            
            // Extract parties from municipalities - prefix IDs with election ID
            for (Constituency constituency : election.getConstituencies()) {
                for (Municipality municipality : constituency.getMunicipalities()) {
                    for (Party party : municipality.getAllParties()) {
                        String uniquePartyId = electionId + "-" + party.getId();
                        if (election.getPartyById(uniquePartyId) == null) {
                            election.addParty(new Party(uniquePartyId, party.getName()));
                        }
                    }
                }
            }
            
            // Filter out any National objects with null type before saving
            List<National> validNationals = election.getNationalVotes().stream()
                .filter(n -> n != null && n.getType() != null)
                .collect(Collectors.toList());
            
            int invalidCount = election.getNationalVotes().size() - validNationals.size();
            if (invalidCount > 0) {
                System.out.println("Filtered out " + invalidCount + " National objects with null type");
            }
            
            // Clear and re-add only valid nationals
            election.clearNationalVotes();
            validNationals.forEach(election::addNationalVotes);
            
            // Save to database - DIRECT save, no separate method call
            System.out.println("Saving election to database...");
            Election savedElection = electionRepository.save(election);
            electionRepository.flush(); // Force immediate write
            
            System.out.println("✓ Election " + savedElection.getId() + " saved to database");
            System.out.println("  - Constituencies: " + savedElection.getConstituencies().size());
            System.out.println("  - Parties: " + savedElection.getParties().size());
            System.out.println("  - Candidates: " + savedElection.getCandidates().size());
            System.out.println("  - National votes: " + savedElection.getNationalVotes().size());

            // Add to cache
            electionCache.put(electionId, new CacheEntry(savedElection, Instant.now()));

            return savedElection;
            
        } catch (IOException | XMLStreamException | NullPointerException | ParserConfigurationException |
                 SAXException e) {
            System.err.println("ERROR: Failed to process election results for " + electionId);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets an election by ID. First checks cache, then database.
     */
    @Transactional(readOnly = true)
    public List<String> getAllElectionIds() {
        return electionRepository.findAll().stream()
            .map(Election::getId)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Election getElectionById(String electionId) {
        // First check cache
        CacheEntry entry = electionCache.get(electionId);
        if (entry != null && !entry.isExpired(CACHE_EXPIRATION_HOURS)) {
            System.out.println("Returning election " + electionId + " from cache");
            return entry.getElection();
        }
        
        // Load from database
        System.out.println("Loading election " + electionId + " from database...");
        return electionRepository.findByIdWithDetails(electionId)
            .map(election -> {
                // Initialize all lazy collections to prevent LazyInitializationException
                election.getConstituencies().size();
                election.getParties().size();
                election.getCandidates().size();
                election.getNationalVotes().size();
                election.getSeatAllocations().size();
                
                // Initialize nested lazy collections
                for (Constituency c : election.getConstituencies()) {
                    c.getMunicipalities().size();
                    for (Municipality m : c.getMunicipalities()) {
                        // Initialize municipality partyVotes
                        m.getAllParties().size();
                        m.getPollingStations().size();
                        // Initialize polling station votes
                        for (PollingStation ps : m.getPollingStations()) {
                            ps.getAllParties().size();
                        }
                    }
                }
                
                // Cache it
                electionCache.put(electionId, new CacheEntry(election, Instant.now()));
                System.out.println("Election " + electionId + " loaded and cached");
                return election;
            })
            .orElse(null);
    }

    /**
     * Caches an election in the service cache.
     */
    public void cacheElection(String electionId, Election election) {
        electionCache.put(electionId, new CacheEntry(election, Instant.now()));
    }

    /**
     * Loads candidate lists and vote totals for an election.
     */
    @Transactional
    public void loadCandidateLists(Election election, String folderName) {
        String electionId = election.getId().trim();
        folderName = folderName.trim();

        // Check if candidates already exist
        CacheEntry cachedEntry = electionCache.get(electionId);
        if (cachedEntry != null && !cachedEntry.isExpired(CACHE_EXPIRATION_HOURS)) {
            Election cachedElection = cachedEntry.getElection();
            if (cachedElection != null && !cachedElection.getCandidates().isEmpty()) {
                System.out.println("Candidates already cached for: " + electionId);
                return;
            }
        }
        
        // Check database
        List<Candidate> dbCandidates = candidateRepository.findByElectionId(electionId);
        if (!dbCandidates.isEmpty()) {
            System.out.println("Candidates already in database for: " + electionId + " (" + dbCandidates.size() + ")");
            return;
        }

        System.out.println("Parsing candidate lists for: " + electionId);

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
            String resourcePath = PathUtils.getResourcePath("/" + safeFolderName);
            
            if (resourcePath == null) {
                System.err.println("ERROR: Could not find resource path for folder: " + folderName);
                return;
            }

            // Parse candidate lists
            electionParser.parseCandidateListsAndTotalVotes(electionId, resourcePath);
            
            System.out.println("Loaded " + election.getCandidates().size() + " candidates");
            
            // Save updated election
            electionRepository.save(election);
            electionRepository.flush();

            electionCache.put(electionId, new CacheEntry(election, Instant.now()));
            System.out.println("✓ Candidates saved for: " + electionId);

        } catch (IOException | XMLStreamException | ParserConfigurationException |
                 SAXException | NullPointerException e) {
            System.err.println("ERROR: Failed to load candidate lists for " + electionId);
            e.printStackTrace();
        }
    }
}
