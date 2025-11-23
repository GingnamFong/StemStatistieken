package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Just prints to content of electionData to the standard output.>br/>
 * <b>This class needs heavy modification!</b>
 */
public class DutchMunicipalityVotesTransformer implements VotesTransformer {
    /** The election instance to which data will be added. */
    private final Election election;

    /**
     * Creates a new transformer for registering municipality vote data
     * into the given election model.
     *
     * @param election the election object where parsed data will be stored
     */
    public DutchMunicipalityVotesTransformer(Election election) {
        this.election = election;
    }

    /**
     * Registers aggregated party-level votes for a municipality.
     * <p>
     * The method extracts municipality identifiers, party information, and
     * vote counts from the provided {@code electionData} map and inserts them
     * into the correct {@link Constituency} and {@link Municipality} within
     * the {@link Election}. If a municipality does not yet exist, it is created.
     * </p>
     *
     * @param aggregated   whether this entry contains aggregated vote data
     * @param electionData a map of XML key-value pairs representing election data
     */
    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> data) {

        // 1) Municipality-level aggregated votes
        if (aggregated) {
            handleMunicipalityVotes(data);
        }

        // 2) Polling-station votes (SB entries)
        handlePollingStationVotes(data);
    }

    // ---------------------------------------------------------
    // MUNICIPALITY (aggregated = true)
    // ---------------------------------------------------------
    private void handleMunicipalityVotes(Map<String, String> data) {

        String municipalityName = data.getOrDefault("AuthorityIdentifier", "unknown");
        String municipalityId = data.getOrDefault("AuthorityIdentifier-Id", "unknown");
        String constituencyId = data.getOrDefault("ContestIdentifier-Id", "unknown");

        String partyId = data.getOrDefault("AffiliationIdentifier-Id", "unknown");
        String partyName = data.getOrDefault("RegisteredName", "unknown");
        int validVotes = Integer.parseInt(data.getOrDefault("ValidVotes", "0"));

        Constituency constituency = election.getConstituencyById(constituencyId);
        if (constituency == null) return;

        Municipality municipality = constituency.getMunicipalityById(municipalityId);
        if (municipality == null) {
            municipality = new Municipality(municipalityId, municipalityName, 0);
            constituency.addMunicipality(municipality);
        }

        municipality.addVotesForParty(partyId, partyName, validVotes);
    }

    // ---------------------------------------------------------
    // POLLING STATION (SB entries)  — aggregated = false
    // ---------------------------------------------------------
    private void handlePollingStationVotes(Map<String, String> data) {

        String reportingUnit = data.get("ReportingUnitIdentifier");
        String reportingUnitId = data.get("ReportingUnitIdentifier-Id");

        if (reportingUnit == null || reportingUnitId == null) return;

        // detect SB entries robustly
        if (!reportingUnitId.matches(".*SB\\d+.*")) return;

        System.out.println("SB FOUND: " + reportingUnitId + " | " + reportingUnit);

        // extract municipality ID
        String municipalityId = extractMunicipalityId(reportingUnitId);
        System.out.println(" → MUNI ID: " + municipalityId);

        if (municipalityId == null) {
            System.out.println(" ⚠️ Could not extract municipality ID from " + reportingUnitId);
            return;
        }

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) {
            System.out.println(" ⚠️ Municipality NOT FOUND for SB " + reportingUnitId);
            return;
        }

        String stationName = reportingUnit;
        String postalCode = extractPostalCode(reportingUnit);

        PollingStation station = municipality.getPollingStationById(reportingUnitId);
        if (station == null) {
            station = new PollingStation(reportingUnitId, stationName, postalCode);
            municipality.addPollingStation(station);
            System.out.println(" ➕ Added SB: " + reportingUnitId + " (postal=" + postalCode + ")");
        }

        String partyId = data.get("AffiliationIdentifier-Id");
        String partyName = data.get("RegisteredName");
        int votes = Integer.parseInt(data.getOrDefault("ValidVotes", "0"));

        station.addVotes(partyId, partyName, votes);
    }

    private String extractMunicipalityId(String stationId) {
        if (stationId == null) return null;

        var m = Pattern.compile("^(\\d{4})").matcher(stationId);
        return m.find() ? m.group(1) : null;
    }




    // ---------------------------------------------------------
    // Extract postal code from strings like:
    // "Stembureau Gemeentehuis (postcode: 1431BZ)"
    // ---------------------------------------------------------
    private String extractPostalCode(String text) {
        if (text == null) return null;

        // Matches everything like: (postcode: 1011 PN)
        var m = java.util.regex.Pattern
                .compile("\\(postcode\\s*:\\s*([0-9A-Z]{4}\\s*[A-Z]{2})\\)", Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (m.find()) {
            return m.group(1).replace(" ", "").toUpperCase();
        }

        return null;
    }



    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Not needed here
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> electionData) {
        // Not needed here
    }
}