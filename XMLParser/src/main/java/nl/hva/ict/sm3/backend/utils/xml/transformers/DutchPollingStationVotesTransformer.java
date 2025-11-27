
package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Transformer voor het verwerken van stembureau stemmen.
 * Verwerkt SB entries uit XML data en registreert stemmen per stembureau.
 */
public class DutchPollingStationVotesTransformer implements VotesTransformer {
    /** De verkiezing instantie waaraan data wordt toegevoegd. */
    private final Election election;

    /**
     * @param election het verkiezingsobject waar data wordt opgeslagen
     */
    public DutchPollingStationVotesTransformer(Election election) {
        this.election = election;
    }

    /**
     * Registreert stemmen voor partijen op stembureau niveau.
     * Detecteert SB entries en maakt stembureaus aan indien nodig.
     *
     * @param aggregated niet gebruikt voor stembureaus
     * @param data XML verkiezingsdata
     */
    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> data) {
        handlePollingStationVotes(data);
    }

    /**
     * Verwerkt stembureau stemmen (SB entries).
     *
     * @param data verkiezingsdata
     */
    private void handlePollingStationVotes(Map<String, String> data) {
        String reportingUnit = data.get("ReportingUnitIdentifier");
        String reportingUnitId = data.get("ReportingUnitIdentifier-Id");

        if (reportingUnit == null || reportingUnitId == null) return;

        // Detecteer SB entries robuust
        if (!reportingUnitId.matches(".*SB\\d+.*")) return;

        // Extraheer gemeente ID
        String municipalityId = extractMunicipalityId(reportingUnitId);
        if (municipalityId == null) {
            return;
        }

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) {
            return;
        }

        String stationName = reportingUnit;
        String postalCode = extractPostalCode(reportingUnit);

        PollingStation station = municipality.getPollingStationById(reportingUnitId);
        if (station == null) {
            station = new PollingStation(reportingUnitId, stationName, postalCode);
            municipality.addPollingStation(station);
        }

        String partyId = data.get("AffiliationIdentifier-Id");
        String partyName = data.get("RegisteredName");
        int votes = Integer.parseInt(data.getOrDefault("ValidVotes", "0"));

        station.addVotes(partyId, partyName, votes);
        System.out.println("PS-DATA: " + data);
    }

    /**
     * Extraheert gemeente ID uit stembureau ID (eerste 4 cijfers).
     * Bijvoorbeeld: "0363SB001" → "0363"
     *
     * @param stationId stembureau ID
     * @return gemeente ID of null
     */
    private String extractMunicipalityId(String stationId) {
        if (stationId == null) return null;

        var m = Pattern.compile("^(\\d{4})").matcher(stationId);
        return m.find() ? m.group(1) : null;
    }

    /**
     * Extraheert postcode uit tekst (bijv. "Stembureau (postcode: 1431BZ)").
     *
     * @param text tekst met postcode
     * @return postcode zonder spaties in hoofdletters, of null
     */
    private String extractPostalCode(String text) {
        if (text == null) return null;

        // Match alles zoals: (postcode: 1011 PN)
        var m = Pattern
                .compile("\\(postcode\\s*:\\s*([0-9A-Z]{4}\\s*[A-Z]{2})\\)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (m.find()) {
            return m.group(1).replace(" ", "").toUpperCase();
        }

        return null;
    }

    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Interface vereist, maar niet gebruikt voor stembureaus
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Interface vereist, maar niet gebruikt voor stembureaus
    }
}
