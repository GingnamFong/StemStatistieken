package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
/**
 * Service class that provides lookup utilities for municipalities and
 * polling stations within an {@link Election}.
 *
 * <p>The service operates on the hierarchical election structure:
 *
 * <pre>
 * Election → Constituency → Municipality → PollingStation
 * </pre>
 *
 * <p>Main features:</p>
 * <ul>
 *     <li>Search polling stations by (normalized) postal code</li>
 *     <li>Find the municipality that contains a polling station for a given postal code</li>
 *     <li>Supports inconsistent postal code formatting (spaces, lowercase)</li>
 * </ul>
 */
public class MunicipalityService {

    private final Election election;

    /**
     * Creates a new {@code MunicipalityService} instance for a given election.
     *
     * @param election the election data used for queries
     */
    public MunicipalityService(Election election) {
        this.election = election;
    }
    /**
     * Finds a polling station by its postal code.
     *
     * <p><b>Normalization rule:</b></p>
     * <ul>
     *     <li>Spaces are removed</li>
     *     <li>Letters are converted to uppercase</li>
     *     <li>Example: {@code "12 34 ab"} → {@code "1234AB"}</li>
     * </ul>
     *
     * <p>The method iterates through all constituencies, municipalities,
     * and polling stations until a match is found.</p>
     *
     * @param postalCode the input postal code (may contain spaces or lowercase letters)
     * @return the matching {@link PollingStation}, or {@code null} if none exists
     */

    public PollingStation findPollingStationByPostalCode(String postalCode) {
        String normalized = postalCode.replace(" ", "").toUpperCase();

        for (Constituency c : election.getConstituencies()) {
            for (Municipality m : c.getMunicipalities()) {
                for (PollingStation ps : m.getPollingStations()) {

                    if (ps.getPostalCode() == null) continue;

                    String normalizedPs = ps.getPostalCode()
                            .replace(" ", "")
                            .toUpperCase();

                    if (normalizedPs.equals(normalized)) {
                        System.out.println("MATCH FOUND: " + normalized);
                        return ps;
                    }
                }
            }
        }

        System.out.println("NO MATCH FOR " + normalized);
        return null;
    }
    /**
     * Finds the municipality that contains a polling station with the given postal code.
     *
     * <p>This method does NOT normalize postal codes fully; it performs
     * a case-insensitive comparison. It assumes postal code formats inside
     * the election dataset are fairly consistent.</p>
     *
     * <p>Use this method when you want to know the municipality to which a
     * postal code belongs.</p>
     *
     * @param postalCode the postal code of the polling station
     * @return the {@link Municipality} containing that polling station,
     *         or {@code null} if no such station exists
     */

    public Municipality findMunicipalityByPostalCode(String postalCode) {
        if (postalCode == null) return null;

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
}
