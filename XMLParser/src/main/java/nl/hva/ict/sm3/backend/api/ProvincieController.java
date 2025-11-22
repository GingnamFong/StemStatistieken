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
 * REST-controller voor provincies.
 * Biedt endpoints om provincies, resultaten en kieskringen op te halen.
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
     * GET /provincies?electionId=TK2021
     * Haalt alle provincies op (basisinfo) voor een specifieke verkiezing.

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
     * GET /provincies/{provincieNaam}?electionId=TK2021
     * Haalt een specifieke provincie op inclusief resultaten voor een specifieke verkiezing.
     
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
     * GET /provincies/{provincieNaam}/resultaten?electionId=TK2021
     * Haalt de uitslag (resultaten) op van een provincie voor een specifieke verkiezing.

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
     * GET /provincies/{provincieNaam}/kieskringen
     * Haalt alle kieskringen (constituencies) binnen een provincie op.

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
     * Valideert de meegegeven provincienaam.

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
