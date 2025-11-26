package nl.hva.ict.sm3.backend.mapper;

import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper voor provincie data.
 * Zet Java objecten om naar JSON formaat voor de API responses.
 */
public class ProvinceMapper {
    
    /**
     * Zet provincie om naar basis JSON formaat (zonder resultaten).
     * 
     * @param province Provincie object
     * @return Map met naam en kieskringen
     */
    public static Map<String, Object> toBasicDTO(Province province) {
        Map<String, Object> map = new HashMap<>();
        map.put("naam", province.getName());
        map.put("kieskringen", province.getConstituencyIds());
        return map;
    }
    
    /**
     * Zet provincie om naar JSON formaat inclusief verkiezingsresultaten.
     * 
     * @param province Provincie object
     * @return Map met naam, kieskringen en resultaten
     */
    public static Map<String, Object> toDTOWithResults(Province province) {
        Map<String, Object> map = toBasicDTO(province);
        ProvinceResults results = new ProvinceResults(province.getTotalVotes(), province.getParties());
        map.put("resultaten", toResultsDTO(results));
        return map;
    }
    
    /**
     * Zet alleen verkiezingsresultaten om naar JSON formaat.
     * 
     * @param results ProvinceResults object
     * @return Map met totaalStemmen en partijen lijst
     */
    public static Map<String, Object> toResultsDTO(ProvinceResults results) {
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
    
    /**
     * Zet kieskring informatie om naar JSON formaat.
     * 
     * @param kieskringNaam Naam van de kieskring
     * @param provincieNaam Naam van de provincie
     * @return Map met kieskring naam en provincie
     */
    public static Map<String, Object> toConstituencyDTO(String kieskringNaam, String provincieNaam) {
        Map<String, Object> map = new HashMap<>();
        map.put("naam", kieskringNaam);
        map.put("provincie", provincieNaam);
        return map;
    }
}

