package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for provinces.
 * Retrieves province data and aggregates election results from constituencies.
 * Provinces are built by combining all municipalities in the corresponding constituencies.
 */
@Service
public class ProvincieService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProvincieService.class);
    
    private final Map<String, List<String>> provincieKieskringenMap;
    // Cache per election: electionId -> (provinceName -> Province)
    private final Map<String, Map<String, Province>> electionProvinceCache = new HashMap<>();
    private final DutchElectionService electionService;

    public ProvincieService(DutchElectionService electionService) {
        this.electionService = electionService;
        this.provincieKieskringenMap = new HashMap<>();
        provincieKieskringenMap.put("Groningen", List.of("Groningen"));
        provincieKieskringenMap.put("Friesland", List.of("Leeuwarden"));
        provincieKieskringenMap.put("Drenthe", List.of("Assen"));
        provincieKieskringenMap.put("Overijssel", List.of("Zwolle"));
        provincieKieskringenMap.put("Flevoland", List.of("Lelystad"));
        provincieKieskringenMap.put("Gelderland", List.of("Nijmegen", "Arnhem"));
        provincieKieskringenMap.put("Utrecht", List.of("Utrecht"));
        provincieKieskringenMap.put("Noord-Holland", List.of("Amsterdam", "Haarlem", "Den_Helder"));
        provincieKieskringenMap.put("Zuid-Holland", List.of("s-Gravenhage", "Rotterdam", "Dordrecht", "Leiden"));
        provincieKieskringenMap.put("Zeeland", List.of("Middelburg"));
        provincieKieskringenMap.put("Noord-Brabant", List.of("Tilburg", "s-Hertogenbosch"));
        provincieKieskringenMap.put("Limburg", List.of("Maastricht"));
        logger.info("ProvincieService initialized with {} provinces", provincieKieskringenMap.size());
    }

    /**
     * Retrieves all provinces for an election.
     * 
     * @param electionId Election ID (e.g. TK2021)
     * @return List of all provinces
     */
    public List<Province> getAllProvinciesForElection(String electionId) {
        logger.debug("Retrieving all provinces for election: {}", electionId);
        return provincieKieskringenMap.keySet().stream()
                .map(name -> getOrCreateProvinceForElection(electionId, name))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves province data including election results.
     * Automatically loads results if they are not yet loaded.
     * 
     * @param electionId Election ID (e.g. TK2021)
     * @param provincieNaam Name of the province
     * @return Province object with results
     */
    public Province getProvincieDataForElection(String electionId, String provincieNaam) {
        logger.debug("Retrieving data for province: {} in election: {}", provincieNaam, electionId);
        
        Province province = getOrCreateProvinceForElection(electionId, provincieNaam);
        if (province.getParties().isEmpty()) {
            logger.debug("Loading election results for province: {} in election: {}", provincieNaam, electionId);
            loadProvinceResultsForElection(electionId, province);
        }
        return province;
    }

    /**
     * Retrieves only the election results of a province.
     * 
     * @param electionId Election ID (e.g. TK2021)
     * @param provincieNaam Name of the province
     * @return ProvinceResults with total votes and parties
     */
    public ProvinceResults getProvincieResultatenForElection(String electionId, String provincieNaam) {
        logger.debug("Retrieving results for province: {} in election: {}", provincieNaam, electionId);
        
        Province province = getProvincieDataForElection(electionId, provincieNaam);
        return new ProvinceResults(province.getTotalVotes(), province.getParties());
    }

    /**
     * Returns all constituencies that belong to a province.
     * 
     * @param provincieNaam Name of the province
     * @return List of constituency names
     */
    public List<String> getKieskringenInProvincie(String provincieNaam) {
        logger.debug("Retrieving constituencies for province: {}", provincieNaam);
        
        List<String> kieskringen = provincieKieskringenMap.get(provincieNaam);
        return kieskringen != null ? List.copyOf(kieskringen) : List.of();
    }

    /**
     * Retrieves province from cache or creates a new one for an election.
     * 
     * @param electionId Election ID
     * @param provincieNaam Name of the province
     * @return Province object
     */
    private Province getOrCreateProvinceForElection(String electionId, String provincieNaam) {
        Map<String, Province> electionCache = electionProvinceCache.computeIfAbsent(electionId, k -> new HashMap<>());
        return electionCache.computeIfAbsent(provincieNaam, name -> {
            logger.debug("Creating new Province instance for: {} in election: {}", name, electionId);
            Province province = new Province(name);
            List<String> kieskringen = provincieKieskringenMap.get(name);
            if (kieskringen != null) {
                kieskringen.forEach(province::addConstituencyId);
            }
            return province;
        });
    }

    /**
     * Loads and aggregates province results from Election data.
     * Iterates through all constituencies of the province and sums votes from all municipalities.
     * 
     * @param electionId Election ID
     * @param province Province object to add results to
     */
    private void loadProvinceResultsForElection(String electionId, Province province) {
        // Get the Election (loads automatically if not yet in cache)
        Election election = Optional.ofNullable(electionService.getElectionById(electionId))
                .orElseGet(() -> electionService.readResults(electionId, electionId));
        
        if (election == null) {
            logger.error("Could not load election: {}", electionId);
            return;
        }

        // Get the constituency names that belong to this province
        List<String> kieskringNamen = provincieKieskringenMap.get(province.getName());
        if (kieskringNamen == null || kieskringNamen.isEmpty()) {
            logger.warn("No constituencies found for province: {}", province.getName());
            return;
        }

        logger.debug("Aggregating votes from {} constituencies for province: {} in election: {}", 
                kieskringNamen.size(), province.getName(), electionId);

        // For each constituency that belongs to this province
        for (String kieskringNaam : kieskringNamen) {
            // Find the constituency in the Election data (match on ID or name, case-insensitive)
            Constituency constituency = election.getConstituencies().stream()
                    .filter(c -> {
                        String cName = c.getName() != null ? c.getName().trim() : "";
                        String cId = c.getId() != null ? c.getId().trim() : "";
                        String searchName = kieskringNaam.trim();
                        return cName.equalsIgnoreCase(searchName) || 
                               cId.equalsIgnoreCase(searchName) ||
                               cName.replace("_", "").equalsIgnoreCase(searchName.replace("_", ""));
                    })
                    .findFirst()
                    .orElse(null);

            if (constituency == null) {
                logger.warn("Constituency '{}' not found in election {} for province {}. Available: {}", 
                        kieskringNaam, electionId, province.getName(),
                        election.getConstituencies().stream()
                                .map(c -> c.getName() + "(" + c.getId() + ")")
                                .collect(Collectors.joining(", ")));
                continue;
            }

            // Aggregate all parties from all municipalities in this constituency
            constituency.getMunicipalities().stream()
                    .flatMap(municipality -> municipality.getAllParties().stream())
                    .forEach(party -> province.addPartyVotes(party.getId(), party.getName(), party.getVotes()));
        }

        province.calculateTotalVotes();
        logger.debug("Loaded results for province {} in election {}: {} parties, {} total votes", 
                province.getName(), electionId, province.getParties().size(), province.getTotalVotes());
    }

}
