package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;
import java.util.regex.Matcher;
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
        // Municipality-level aggregated votes
        if (aggregated) {
            handleMunicipalityVotes(data);
        }
        handleSBPartyVotes(data);
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
    private void handleSBPartyVotes(Map<String, String> data) {

        String stationId = data.get("ReportingUnitIdentifier-Id");
        if (stationId == null || !stationId.contains("SB")) return;

        String fullName = data.get("ReportingUnitIdentifier");
        if (fullName == null) return;

        Matcher matcher = Pattern
                .compile("^(.*?) \\(postcode: (.*?)\\)$")
                .matcher(fullName);

        if (!matcher.find()) return;

        String stationName = matcher.group(1);
        String postalCode = matcher.group(2).replace(" ", "").toUpperCase();

        String partyId = data.get("AffiliationIdentifier-Id");
        String partyName = data.get("RegisteredName");
        int votes = Integer.parseInt(data.getOrDefault("ValidVotes", "0"));

        if (partyId == null || partyName == null) return;

        // Municipality ID extracted from prefix (e.g. "0363" from "0363::SB1")
        String municipalityId = stationId.substring(0, 4);

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return;

        PollingStation station = municipality.getPollingStationById(stationId);
        if (station == null) {
            station = new PollingStation(stationId, stationName, postalCode);
            municipality.addPollingStation(station);
        }

        station.addVotes(partyId, partyName, votes);
    }


    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> electionData) {
        // Not needed here
    }

    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> data) {

        if (aggregated) {
            // could be used for storing municipality cast/rejected, but your model doesn't store it
            return;
        }

        // -------- SB LEVEL METADATA --------

        String stationId = data.get("ReportingUnitIdentifier-Id");
        String fullName = data.get("ReportingUnitIdentifier");

        if (stationId == null || fullName == null || !stationId.contains("SB")) return;

        Matcher matcher = Pattern
                .compile("^(.*?) \\(postcode: (.*?)\\)$")
                .matcher(fullName);

        if (!matcher.find()) return;

        String stationName = matcher.group(1);
        String postalCode = matcher.group(2).replace(" ", "").toUpperCase();

        int cast = Integer.parseInt(data.getOrDefault("Cast", "0"));

        int rejected = data.entrySet().stream()
                .filter(e -> e.getKey().startsWith("RejectedVotes"))
                .mapToInt(e -> Integer.parseInt(e.getValue()))
                .sum();

        // Municipality ID prefix
        String municipalityId = stationId.substring(0, 4);

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return;

        // Find or create SB
        PollingStation station = municipality.getPollingStationById(stationId);
        if (station == null) {
            station = new PollingStation(stationId, stationName, postalCode);
            municipality.addPollingStation(station);
        }
    }
}
