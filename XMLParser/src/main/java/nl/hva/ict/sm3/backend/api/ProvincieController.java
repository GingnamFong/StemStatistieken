package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.mapper.ProvinceMapper;
import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;
import nl.hva.ict.sm3.backend.service.ProvincieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for provinces.
 * Provides HTTP endpoints to retrieve province data.
 * Processes requests from the frontend and returns JSON responses.
 */
@RestController
@RequestMapping("/provincies")
public class ProvincieController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProvincieController.class);
    
    private final ProvincieService provincieService;

    public ProvincieController(ProvincieService provincieService) {
        this.provincieService = provincieService;
    }

    /**
     * Retrieves all provinces for an election.
     * GET /provincies?electionId=TK2021
     * 
     * @param electionId Election ID (e.g. TK2021)
     * @return List of provinces with basic information
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProvincies(
            @RequestParam(required = false, defaultValue = "TK2023") String electionId) {
        logger.info("GET /provincies?electionId={} - Retrieving all provinces", electionId);
        List<Province> provinces = provincieService.getAllProvinciesForElection(electionId);
        List<Map<String, Object>> provinceMaps = provinces.stream()
                .map(ProvinceMapper::toBasicDTO)
                .collect(Collectors.toList());
        logger.debug("Returning {} provinces", provinceMaps.size());
        return ResponseEntity.ok(provinceMaps);
    }

    /**
     * Retrieves a province including election results.
     * GET /provincies/{provincieNaam}?electionId=TK2021
     * 
     * @param provincieNaam Name of the province
     * @param electionId Election ID (e.g. TK2021)
     * @return Province data with results
     */
    @GetMapping("/{provincieNaam}")
    public ResponseEntity<Map<String, Object>> getProvincieData(
            @PathVariable String provincieNaam,
            @RequestParam(required = false, defaultValue = "TK2023") String electionId) {
        
        validateProvinceName(provincieNaam);
        logger.info("GET /provincies/{}?electionId={} - Retrieving province data", provincieNaam, electionId);
        
        Province province = provincieService.getProvincieDataForElection(electionId, provincieNaam);
        return ResponseEntity.ok(ProvinceMapper.toDTOWithResults(province));
    }

    /**
     * Retrieves only the election results of a province.
     * GET /provincies/{provincieNaam}/resultaten?electionId=TK2021
     * 
     * @param provincieNaam Name of the province
     * @param electionId Election ID (e.g. TK2021)
     * @return Results with total votes and parties
     */
    @GetMapping("/{provincieNaam}/resultaten")
    public ResponseEntity<Map<String, Object>> getProvincieResultaten(
            @PathVariable String provincieNaam,
            @RequestParam(required = false, defaultValue = "TK2023") String electionId) {
        
        validateProvinceName(provincieNaam);
        logger.info("GET /provincies/{}/resultaten?electionId={} - Retrieving province results", provincieNaam, electionId);
        
        ProvinceResults results = provincieService.getProvincieResultatenForElection(electionId, provincieNaam);
        return ResponseEntity.ok(ProvinceMapper.toResultsDTO(results));
    }

    /**
     * Retrieves all constituencies that belong to a province.
     * GET /provincies/{provincieNaam}/kieskringen
     * 
     * @param provincieNaam Name of the province
     * @return List of constituencies in this province
     */
    @GetMapping("/{provincieNaam}/kieskringen")
    public ResponseEntity<List<Map<String, Object>>> getKieskringenInProvincie(
            @PathVariable String provincieNaam) {
        
        validateProvinceName(provincieNaam);
        logger.info("GET /provincies/{}/kieskringen - Retrieving constituencies", provincieNaam);
        
        List<String> kieskringen = provincieService.getKieskringenInProvincie(provincieNaam);
        List<Map<String, Object>> kieskringMaps = kieskringen.stream()
                .map(kieskring -> ProvinceMapper.toConstituencyDTO(kieskring, provincieNaam))
                .collect(Collectors.toList());
        logger.debug("Returning {} constituencies for province {}", kieskringMaps.size(), provincieNaam);
        return ResponseEntity.ok(kieskringMaps);
    }

    /**
     * Validates the provided province name.
     *
     * @param provincieNaam Province name to validate
     * @throws IllegalArgumentException if the name is invalid
     */
    private void validateProvinceName(String provincieNaam) {
        if (provincieNaam == null || provincieNaam.trim().isEmpty()) {
            logger.warn("Province name validation failed: null or empty");
            throw new IllegalArgumentException("Province name cannot be null or empty");
        }
        String sanitized = provincieNaam.trim();
        if (sanitized.contains("<") || sanitized.contains(">") || sanitized.contains("&")) {
            logger.warn("Province name contains potentially dangerous characters: {}", provincieNaam);
            throw new IllegalArgumentException("Province name contains invalid characters");
        }
    }
}
