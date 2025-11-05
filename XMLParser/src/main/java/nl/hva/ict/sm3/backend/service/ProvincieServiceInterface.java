package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Province;
import nl.hva.ict.sm3.backend.model.ProvinceResults;

import java.util.List;

/**
 * Service-interface voor provincies (SOLID): definieert de publieke operaties.
 */
public interface ProvincieServiceInterface {
    /** Geeft alle provincies (basisinfo). */
    List<Province> getAllProvincies();

    /** Geeft één provincie inclusief resultaten. */
    Province getProvincieData(String provincieNaam);

    /** Geeft alleen de resultaten van een provincie. */
    ProvinceResults getProvincieResultaten(String provincieNaam);

    /** Geeft alle kieskringen van een provincie. */
    List<String> getKieskringenInProvincie(String provincieNaam);
}

