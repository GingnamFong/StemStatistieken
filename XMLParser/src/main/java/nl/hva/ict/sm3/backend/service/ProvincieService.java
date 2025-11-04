package nl.hva.ict.sm3.backend.service;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ProvincieService {

    // Mapping van kieskringen naar provincie
    private final Map<String, List<String>> provincieKieskringenMap;

    {
        provincieKieskringenMap = new HashMap<>();
        // Groningen: Groningen
        provincieKieskringenMap.put("Groningen", List.of("Groningen"));

        // Friesland: Leeuwarden  
        provincieKieskringenMap.put("Friesland", List.of("Leeuwarden"));

        // Drenthe: Assen
        provincieKieskringenMap.put("Drenthe", List.of("Assen"));

        // Overijssel: Zwolle
        provincieKieskringenMap.put("Overijssel", List.of("Zwolle"));

        // Flevoland: Lelystad
        provincieKieskringenMap.put("Flevoland", List.of("Lelystad"));

        // Gelderland: Nijmegen + Arnhem
        provincieKieskringenMap.put("Gelderland", List.of("Nijmegen", "Arnhem"));

        // Utrecht: Utrecht
        provincieKieskringenMap.put("Utrecht", List.of("Utrecht"));
        provincieKieskringenMap.put("Noord-Holland", List.of("Amsterdam", "Haarlem", "Den_Helder"));

        // Zuid-Holland: s-Gravenhage + Rotterdam + Dordrecht + Leiden
        provincieKieskringenMap.put("Zuid-Holland", List.of("s-Gravenhage", "Rotterdam", "Dordrecht", "Leiden"));

        // Zeeland: Middelburg
        provincieKieskringenMap.put("Zeeland", List.of("Middelburg"));

        // Noord-Brabant: Tilburg + s-Hertogenbosch
        provincieKieskringenMap.put("Noord-Brabant", List.of("Tilburg", "s-Hertogenbosch"));

        // Limburg: Maastricht
        provincieKieskringenMap.put("Limburg", List.of("Maastricht"));
    }

    public List<Map<String, Object>> getAllProvincies() {
        List<Map<String, Object>> provincies = new ArrayList<>();

        for (String provincieNaam : provincieKieskringenMap.keySet()) {
            Map<String, Object> provincie = new HashMap<>();
            provincie.put("naam", provincieNaam);
            provincie.put("kieskringen", provincieKieskringenMap.get(provincieNaam));
            provincies.add(provincie);
        }

        return provincies;
    }

    public Map<String, Object> getProvincieData(String provincieNaam) {
        List<String> kieskringen = provincieKieskringenMap.get(provincieNaam);
        if (kieskringen == null) {
            return null;
        }

        Map<String, Object> provincieData = new HashMap<>();
        provincieData.put("naam", provincieNaam);
        provincieData.put("kieskringen", kieskringen);
        provincieData.put("resultaten", aggregateProvincieResultaten(kieskringen));

        return provincieData;
    }

    public Map<String, Object> getProvincieResultaten(String provincieNaam) {
        List<String> kieskringen = provincieKieskringenMap.get(provincieNaam);
        if (kieskringen == null) {
            return null;
        }

        return aggregateProvincieResultaten(kieskringen);
    }

    public List<Map<String, Object>> getKieskringenInProvincie(String provincieNaam) {
        List<String> kieskringNamen = provincieKieskringenMap.get(provincieNaam);
        if (kieskringNamen == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> kieskringen = new ArrayList<>();
        for (String kieskringNaam : kieskringNamen) {
            Map<String, Object> kieskring = new HashMap<>();
            kieskring.put("naam", kieskringNaam);
            kieskring.put("provincie", provincieNaam);
            kieskringen.add(kieskring);
        }

        return kieskringen;
    }

    private Map<String, Object> aggregateProvincieResultaten(List<String> kieskringen) {
        Map<String, Integer> partijStemmen = new HashMap<>();

        for (String kieskring : kieskringen) {
            Map<String, Integer> kieskringStemmen = parseKieskringXML(kieskring);
            for (Map.Entry<String, Integer> entry : kieskringStemmen.entrySet()) {
                partijStemmen.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }

        int totaalStemmen = partijStemmen.values().stream().mapToInt(Integer::intValue).sum();

        List<Map<String, Object>> partijResultaten = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : partijStemmen.entrySet()) {
            Map<String, Object> partij = new HashMap<>();
            partij.put("naam", entry.getKey());
            partij.put("stemmen", entry.getValue());
            partij.put("percentage", String.format("%.1f", (entry.getValue() * 100.0 / totaalStemmen)));
            partijResultaten.add(partij);
        }

        partijResultaten.sort((a, b) ->
            Integer.compare((Integer) b.get("stemmen"), (Integer) a.get("stemmen")));

        Map<String, Object> resultaten = new HashMap<>();
        resultaten.put("totaalStemmen", totaalStemmen);
        resultaten.put("partijen", partijResultaten);
        return resultaten;
    }

    private Map<String, Integer> parseKieskringXML(String kieskring) {
        Map<String, Integer> partijStemmen = new HashMap<>();
        String fileName = "TK2023/Telling_TK2023_kieskring_" + kieskring + ".eml.xml";
        
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) return partijStemmen;
            
            String xmlContent = new String(inputStream.readAllBytes());
            int totalVotesStart = xmlContent.indexOf("<TotalVotes>");
            int totalVotesEnd = xmlContent.indexOf("</TotalVotes>");
            
            if (totalVotesStart == -1 || totalVotesEnd == -1) return partijStemmen;
            
            String totalVotesSection = xmlContent.substring(totalVotesStart, totalVotesEnd);
            int searchPos = 0;
            
            while (true) {
                int affStart = totalVotesSection.indexOf("<AffiliationIdentifier", searchPos);
                if (affStart == -1) break;
                
                int nameStart = totalVotesSection.indexOf("<RegisteredName>", affStart) + 16;
                int nameEnd = totalVotesSection.indexOf("</RegisteredName>", nameStart);
                if (nameEnd == -1) break;
                
                String partijNaam = totalVotesSection.substring(nameStart, nameEnd).trim();
                int affEnd = totalVotesSection.indexOf("</AffiliationIdentifier>", nameEnd);
                if (affEnd == -1) break;
                
                int votesStart = totalVotesSection.indexOf("<ValidVotes>", affEnd);
                if (votesStart == -1 || votesStart > affEnd + 100) {
                    searchPos = affEnd + 1;
                    continue;
                }
                
                int votesEnd = totalVotesSection.indexOf("</ValidVotes>", votesStart + 12);
                if (votesEnd == -1) break;
                
                try {
                    int votes = Integer.parseInt(totalVotesSection.substring(votesStart + 12, votesEnd).trim());
                    partijStemmen.put(partijNaam, votes);
                } catch (NumberFormatException ignored) {}
                
                searchPos = votesEnd + 1;
            }
        } catch (Exception ignored) {}
        
        return partijStemmen;
    }
}

