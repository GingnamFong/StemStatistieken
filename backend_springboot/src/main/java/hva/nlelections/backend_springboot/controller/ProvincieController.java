package hva.nlelections.backend_springboot.controller;

import hva.nlelections.backend_springboot.service.ProvincieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/provincies")
@CrossOrigin(origins = {"http://localhost:5173", "https://hva-frontend.onrender.com"})
public class ProvincieController {

    @Autowired
    private ProvincieService provincieService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllProvincies() {
        try {
            List<Map<String, Object>> provincies = provincieService.getAllProvincies();
            return ResponseEntity.ok(provincies);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{provincieNaam}")
    public ResponseEntity<Map<String, Object>> getProvincieData(@PathVariable String provincieNaam) {
        try {
            Map<String, Object> provincieData = provincieService.getProvincieData(provincieNaam);
            if (provincieData != null) {
                return ResponseEntity.ok(provincieData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{provincieNaam}/resultaten")
    public ResponseEntity<Map<String, Object>> getProvincieResultaten(@PathVariable String provincieNaam) {
        try {
            Map<String, Object> resultaten = provincieService.getProvincieResultaten(provincieNaam);
            if (resultaten != null) {
                return ResponseEntity.ok(resultaten);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{provincieNaam}/kieskringen")
    public ResponseEntity<List<Map<String, Object>>> getKieskringenInProvincie(@PathVariable String provincieNaam) {
        try {
            List<Map<String, Object>> kieskringen = provincieService.getKieskringenInProvincie(provincieNaam);
            return ResponseEntity.ok(kieskringen);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

