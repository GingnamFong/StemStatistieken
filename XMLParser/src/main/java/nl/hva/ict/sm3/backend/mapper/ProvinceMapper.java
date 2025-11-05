package nl.hva.ict.sm3.backend.mapper;

import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simpele mapper: zet domeinobjecten om naar JSON/DTO-structuur voor de API.
 */
public class ProvinceMapper {
    
    public static Map<String, Object> toBasicDTO(Province province) {
        // Zet basisgegevens van een provincie om naar een JSON-vorm
        Map<String, Object> map = new HashMap<>();
        map.put("naam", province.getName());
        map.put("kieskringen", province.getConstituencyIds());
        return map;
    }
    
    public static Map<String, Object> toDTOWithResults(Province province) {
        // Zet provincie om inclusief resultaten (totaal + partijen)
        Map<String, Object> map = toBasicDTO(province);
        ProvinceResults results = new ProvinceResults(province.getTotalVotes(), province.getParties());
        map.put("resultaten", toResultsDTO(results));
        return map;
    }
    
    public static Map<String, Object> toResultsDTO(ProvinceResults results) {
        // Zet resultaten om naar JSON-vorm
        Map<String, Object> map = new HashMap<>();
        map.put("totaalStemmen", results.getTotalVotes());
        
        List<Map<String, Object>> partijen = results.getParties().stream()
                .map(pr -> {
                    Map<String, Object> p = new HashMap<>();
                    p.put("naam", pr.getName());
                    p.put("stemmen", pr.getVotes());
                    p.put("percentage", pr.getPercentageFormatted());
                    return p;
                })
                .collect(Collectors.toList());
        
        map.put("partijen", partijen);
        return map;
    }
    
    public static Map<String, Object> toConstituencyDTO(String kieskringNaam, String provincieNaam) {
        // Zet kieskringinformatie om naar JSON-vorm
        Map<String, Object> map = new HashMap<>();
        map.put("naam", kieskringNaam);
        map.put("provincie", provincieNaam);
        return map;
    }
}

