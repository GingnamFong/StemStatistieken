package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service voor provincies.
 * Haalt provincie data op en aggregeert verkiezingsresultaten uit kieskringen.
 * Provincies worden opgebouwd door alle gemeenten in de bijbehorende kieskringen samen te voegen.
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
     * Haalt alle provincies op voor een verkiezing.
     * 
     * @param electionId Verkiezing ID (bijv. TK2021)
     * @return Lijst van alle provincies
     */
    public List<Province> getAllProvinciesForElection(String electionId) {
        logger.debug("Retrieving all provinces for election: {}", electionId);
        return provincieKieskringenMap.keySet().stream()
                .map(name -> getOrCreateProvinceForElection(electionId, name))
                .collect(Collectors.toList());
    }

    /**
     * Haalt provincie data op inclusief verkiezingsresultaten.
     * Laadt resultaten automatisch als ze nog niet geladen zijn.
     * 
     * @param electionId Verkiezing ID (bijv. TK2021)
     * @param provincieNaam Naam van de provincie
     * @return Provincie object met resultaten
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
     * Haalt alleen de verkiezingsresultaten op van een provincie.
     * 
     * @param electionId Verkiezing ID (bijv. TK2021)
     * @param provincieNaam Naam van de provincie
     * @return ProvinceResults met totaalStemmen en partijen
     */
    public ProvinceResults getProvincieResultatenForElection(String electionId, String provincieNaam) {
        logger.debug("Retrieving results for province: {} in election: {}", provincieNaam, electionId);
        
        Province province = getProvincieDataForElection(electionId, provincieNaam);
        return new ProvinceResults(province.getTotalVotes(), province.getParties());
    }

    /**
     * Geeft alle kieskringen terug die bij een provincie horen.
     * 
     * @param provincieNaam Naam van de provincie
     * @return Lijst van kieskring namen
     */
    public List<String> getKieskringenInProvincie(String provincieNaam) {
        logger.debug("Retrieving constituencies for province: {}", provincieNaam);
        
        List<String> kieskringen = provincieKieskringenMap.get(provincieNaam);
        return kieskringen != null ? new ArrayList<>(kieskringen) : new ArrayList<>();
    }

    /**
     * Haalt provincie uit cache of maakt nieuwe aan voor een verkiezing.
     * 
     * @param electionId Verkiezing ID
     * @param provincieNaam Naam van de provincie
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
     * Laadt en aggregeert provincie resultaten uit Election data.
     * Loopt door alle kieskringen van de provincie en telt stemmen van alle gemeenten bij elkaar op.
     * 
     * @param electionId Verkiezing ID
     * @param province Provincie object om resultaten aan toe te voegen
     */
    private void loadProvinceResultsForElection(String electionId, Province province) {
        // Haal de Election op (laadt automatisch als nog niet in cache)
        Election election = electionService.getElectionById(electionId);
        if (election == null) {
            election = electionService.readResults(electionId, electionId);
            if (election == null) {
                logger.error("Could not load election: {}", electionId);
                return;
            }
        }

        // Haal de kieskring namen op die bij deze provincie horen
        List<String> kieskringNamen = provincieKieskringenMap.get(province.getName());
        if (kieskringNamen == null || kieskringNamen.isEmpty()) {
            logger.warn("No constituencies found for province: {}", province.getName());
            return;
        }

        logger.debug("Aggregating votes from {} constituencies for province: {} in election: {}", 
                kieskringNamen.size(), province.getName(), electionId);

        // Voor elke kieskring die bij deze provincie hoort
        for (String kieskringNaam : kieskringNamen) {
            // Zoek de constituency in de Election data (match op ID of naam, case-insensitive)
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

            // Aggregeer alle partijen uit alle gemeenten in deze kieskring
            for (Municipality municipality : constituency.getMunicipalities()) {
                for (Party party : municipality.getAllParties()) {
                    province.addPartyVotes(party.getId(), party.getName(), party.getVotes());
                }
            }
        }

        province.calculateTotalVotes();
        logger.debug("Loaded results for province {} in election {}: {} parties, {} total votes", 
                province.getName(), electionId, province.getParties().size(), province.getTotalVotes());
    }

}
