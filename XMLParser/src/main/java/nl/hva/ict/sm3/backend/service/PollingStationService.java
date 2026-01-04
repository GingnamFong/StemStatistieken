package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for handling polling station lookups.
 *
 * <p>This service provides methods to find polling stations and their associated municipalities
 * within a given election hierarchy.</p>
 */
@Service
public class PollingStationService {

    private final DutchElectionService electionService;

    public PollingStationService(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    /**
     * Finds a polling station by its postal code for a given election ID.
     *
     * <p>Input normalization:
     * <ul>
     *     <li>Removes spaces</li>
     *     <li>Converts letters to uppercase</li>
     * </ul>
     *
     * @param electionId the ID of the election to search in
     * @param postalCode the postal code to search for
     * @return the polling station, or null if none found
     */
    public PollingStation findByPostalCode(String electionId, String postalCode) {
        if (postalCode == null) return null;

        Election election = electionService.getElectionById(electionId);
        if (election == null) return null;

        String normalized = postalCode.replace(" ", "").toUpperCase();

        for (var constituency : election.getConstituencies()) {
            for (var municipality : constituency.getMunicipalities()) {
                for (var station : municipality.getPollingStations()) {
                    if (station.getPostalCode() == null) continue;
                    String normalizedPs = station.getPostalCode().replace(" ", "").toUpperCase();
                    if (normalizedPs.equals(normalized)) {
                        return station;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the municipality that contains a polling station with the given postal code.
     *
     * @param electionId the election ID
     * @param postalCode the postal code
     * @return the municipality containing the polling station, or null if none found
     */
    public Municipality findMunicipalityByPostalCode(String electionId, String postalCode) {
        if (postalCode == null) return null;

        Election election = electionService.getElectionById(electionId);
        if (election == null) return null;

        for (var constituency : election.getConstituencies()) {
            for (var municipality : constituency.getMunicipalities()) {
                for (var station : municipality.getPollingStations()) {
                    if (postalCode.equalsIgnoreCase(station.getPostalCode())) {
                        return municipality;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns all polling stations in the election.
     *
     * @param electionId the election ID
     * @return list of all polling stations, empty if election not found
     */
    public List<PollingStation> getAllPollingStations(String electionId) {
        Election election = electionService.getElectionById(electionId);
        if (election == null) return List.of();

        return election.getConstituencies().stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .flatMap(m -> m.getPollingStations().stream())
                .toList();
    }
}
