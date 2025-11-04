package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.service.MunicipalityService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/elections/{electionId}/municipalities")
@CrossOrigin(origins = "*")
public class MunicipalityController {

    private final MunicipalityService service;

    public MunicipalityController(MunicipalityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Municipality>> getAll(@PathVariable String electionId) {
        List<Municipality> municipalities = service.getAllMunicipalities(electionId);
        if (municipalities.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(municipalities);
    }

    @GetMapping("/{municipalityId}")
    public ResponseEntity<Municipality> getById(
            @PathVariable String electionId,
            @PathVariable String municipalityId) {
        return service.getMunicipalityById(electionId, municipalityId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
