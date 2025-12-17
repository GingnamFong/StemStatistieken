package nl.hva.ict.sm3.backend.mapper;

import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for province data.
 * Converts Java objects to JSON format for API responses.
 */
public class ProvinceMapper {
    
    /**
     * Converts province to basic JSON format (without results).
     * 
     * @param province Province object
     * @return Map with name and constituencies
     */
    public static Map<String, Object> toBasicDTO(Province province) {
        Map<String, Object> map = new HashMap<>();
        map.put("naam", province.getName());
        map.put("kieskringen", province.getConstituencyIds());
        return map;
    }
    
    /**
     * Converts province to JSON format including election results.
     * 
     * @param province Province object
     * @return Map with name, constituencies and results
     */
    public static Map<String, Object> toDTOWithResults(Province province) {
        Map<String, Object> map = toBasicDTO(province);
        ProvinceResults results = new ProvinceResults(province.getTotalVotes(), province.getParties());
        map.put("resultaten", toResultsDTO(results));
        return map;
    }
    
    /**
     * Converts only election results to JSON format.
     * 
     * @param results ProvinceResults object
     * @return Map with total votes and parties list
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
     * Converts constituency information to JSON format.
     * 
     * @param kieskringNaam Name of the constituency
     * @param provincieNaam Name of the province
     * @return Map with constituency name and province
     */
    public static Map<String, Object> toConstituencyDTO(String kieskringNaam, String provincieNaam) {
        Map<String, Object> map = new HashMap<>();
        map.put("naam", kieskringNaam);
        map.put("provincie", provincieNaam);
        return map;
    }
}

