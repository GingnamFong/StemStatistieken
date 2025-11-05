package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Party;
import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service-laag voor provincies: laden, aggregeren en aanbieden van resultaten.
 */
@Service
public class ProvincieService implements ProvincieServiceInterface {
    
    private static final Logger logger = LoggerFactory.getLogger(ProvincieService.class);
    private static final String XML_RESOURCE_PATH = "TK2023/Telling_TK2023_kieskring_%s.eml.xml";
    
    private final Map<String, List<String>> provincieKieskringenMap;
    private final Map<String, Province> provinceCache = new HashMap<>();

    public ProvincieService() {
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

    public List<Province> getAllProvincies() {
        // Geeft alle provincies terug (met basisinfo)
        logger.debug("Retrieving all provinces");
        return provincieKieskringenMap.keySet().stream()
                .map(this::getOrCreateProvince)
                .collect(Collectors.toList());
    }

    public Province getProvincieData(String provincieNaam) {
        // Geeft één provincie terug inclusief (berekende) resultaten
        logger.debug("Retrieving data for province: {}", provincieNaam);
        
        Province province = getOrCreateProvince(provincieNaam);
        if (province.getParties().isEmpty()) {
            logger.debug("Loading election results for province: {}", provincieNaam);
            loadProvinceResults(province);
        }
        return province;
    }

    public ProvinceResults getProvincieResultaten(String provincieNaam) {
        // Geeft alleen de resultaten (totaal + partijen) van een provincie
        logger.debug("Retrieving results for province: {}", provincieNaam);
        
        Province province = getProvincieData(provincieNaam);
        return new ProvinceResults(province.getTotalVotes(), province.getParties());
    }

    public List<String> getKieskringenInProvincie(String provincieNaam) {
        // Geeft alle kieskringen van een provincie
        logger.debug("Retrieving constituencies for province: {}", provincieNaam);
        
        List<String> kieskringen = provincieKieskringenMap.get(provincieNaam);
        return kieskringen != null ? new ArrayList<>(kieskringen) : new ArrayList<>();
    }

    // Validation is handled in the controller for a minimal setup

    /**
     * Gets or creates a province instance from cache.
     * 
     * @param provincieNaam the province name
     * @return Province instance
     */
    private Province getOrCreateProvince(String provincieNaam) {
        // Haalt provincie uit cache of maakt een nieuwe aan met de juiste kieskringen
        return provinceCache.computeIfAbsent(provincieNaam, name -> {
            logger.debug("Creating new Province instance for: {}", name);
            Province province = new Province(name);
            List<String> kieskringen = provincieKieskringenMap.get(name);
            if (kieskringen != null) {
                kieskringen.forEach(province::addConstituencyId);
            }
            return province;
        });
    }

    /**
     * Loads and aggregates election results for a province from XML files.
     * 
     * @param province the province to load results for
     */
    private void loadProvinceResults(Province province) {
        // Leest stemmen uit XML per kieskring en telt per partij op
        List<String> kieskringen = provincieKieskringenMap.get(province.getName());
        if (kieskringen == null || kieskringen.isEmpty()) {
            logger.warn("No constituencies found for province: {}", province.getName());
            return;
        }

        logger.debug("Aggregating votes from {} constituencies for province: {}", 
                kieskringen.size(), province.getName());

        // Aggregate votes per party across all constituencies using Streams
        kieskringen.stream()
                .map(this::parseKieskringXML)
                .flatMap(votesMap -> votesMap.entrySet().stream())
                .forEach(entry -> {
                    String partyName = entry.getKey();
                    int votes = entry.getValue();
                    String partyId = partyName; // Use party name as ID
                    province.addPartyVotes(partyId, partyName, votes);
                });

        province.calculateTotalVotes();
        logger.debug("Loaded results for province {}: {} parties, {} total votes", 
                province.getName(), province.getParties().size(), province.getTotalVotes());
    }

    /**
     * Parses XML file for a specific constituency (kieskring) and extracts party votes.
     * 
     * @param kieskring the constituency name
     * @return Map of party names to vote counts
     */
    private Map<String, Integer> parseKieskringXML(String kieskring) {
        // Leest XML van één kieskring in en haalt partijstemmen eruit
        String fileName = String.format(XML_RESOURCE_PATH, kieskring);
        Map<String, Integer> partijStemmen = new HashMap<>();
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                logger.warn("XML file not found for constituency: {}", kieskring);
                return partijStemmen;
            }
            
            String xmlContent = new String(inputStream.readAllBytes());
            Map<String, Integer> votes = extractVotesFromXML(xmlContent);
            logger.debug("Parsed {} parties from constituency: {}", votes.size(), kieskring);
            return votes;
            
        } catch (Exception e) {
            logger.error("Error parsing XML for constituency: {}", kieskring, e);
            return partijStemmen;
        }
    }

    /**
     * Extracts party votes from XML content.
     * Separated for better testability and SRP.
     * 
     * @param xmlContent the XML content as string
     * @return Map of party names to vote counts
     */
    private Map<String, Integer> extractVotesFromXML(String xmlContent) {
        // Parser: zoekt per partij de naam en bijbehorende <ValidVotes>
        Map<String, Integer> partijStemmen = new HashMap<>();
        
        int totalVotesStart = xmlContent.indexOf("<TotalVotes>");
        int totalVotesEnd = xmlContent.indexOf("</TotalVotes>");
        
        if (totalVotesStart == -1 || totalVotesEnd == -1) {
            logger.warn("TotalVotes section not found in XML");
            return partijStemmen;
        }
        
        String totalVotesSection = xmlContent.substring(totalVotesStart, totalVotesEnd);
        int searchPos = 0;
        
        while (true) {
            PartyVoteData voteData = extractNextPartyVote(totalVotesSection, searchPos);
            if (voteData == null) {
                break;
            }
            
            partijStemmen.put(voteData.partyName, voteData.votes);
            searchPos = voteData.nextSearchPosition;
        }
        
        return partijStemmen;
    }

    /**
     * Extracts the next party vote from XML section.
     * Uses inner class for better organization.
     * 
     * @param xmlSection the XML section to parse
     * @param startPos the starting position
     * @return PartyVoteData or null if no more parties found
     */
    private PartyVoteData extractNextPartyVote(String xmlSection, int startPos) {
        // Parser-hulp: haalt de volgende partij met stemmen uit de XML-sectie
        int affStart = xmlSection.indexOf("<AffiliationIdentifier", startPos);
        if (affStart == -1) {
            return null;
        }
        
        int nameStart = xmlSection.indexOf("<RegisteredName>", affStart) + 16;
        int nameEnd = xmlSection.indexOf("</RegisteredName>", nameStart);
        if (nameEnd == -1) {
            return null;
        }
        
        String partijNaam = xmlSection.substring(nameStart, nameEnd).trim();
        int affEnd = xmlSection.indexOf("</AffiliationIdentifier>", nameEnd);
        if (affEnd == -1) {
            return null;
        }
        
        int votesStart = xmlSection.indexOf("<ValidVotes>", affEnd);
        if (votesStart == -1 || votesStart > affEnd + 100) {
            return new PartyVoteData(null, 0, affEnd + 1);
        }
        
        int votesEnd = xmlSection.indexOf("</ValidVotes>", votesStart + 12);
        if (votesEnd == -1) {
            return null;
        }
        
        try {
            int votes = Integer.parseInt(xmlSection.substring(votesStart + 12, votesEnd).trim());
            return new PartyVoteData(partijNaam, votes, votesEnd + 1);
        } catch (NumberFormatException e) {
            logger.warn("Invalid vote count format for party: {}", partijNaam);
            return new PartyVoteData(null, 0, votesEnd + 1);
        }
    }

    /**
     * Inner class to hold party vote extraction data.
     */
    private static class PartyVoteData {
        final String partyName;
        final int votes;
        final int nextSearchPosition;

        PartyVoteData(String partyName, int votes, int nextSearchPosition) {
            this.partyName = partyName;
            this.votes = votes;
            this.nextSearchPosition = nextSearchPosition;
        }
    }
}
