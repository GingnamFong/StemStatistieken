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
        String rawMunicipalityId = data.getOrDefault("AuthorityIdentifier-Id", "unknown");
        String rawConstituencyId = data.getOrDefault("ContestIdentifier-Id", "unknown");

        // Prefix IDs with election ID to make unique across elections
        String municipalityId = election.getId() + "-" + rawMunicipalityId;
        String constituencyId = election.getId() + "-" + rawConstituencyId;

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
        // Prefix with election ID
        String rawMunicipalityId = stationId.substring(0, 4);
        String municipalityId = election.getId() + "-" + rawMunicipalityId;
        
        // Station ID also needs to be unique
        String uniqueStationId = election.getId() + "-" + stationId;

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return;

        PollingStation station = municipality.getPollingStationById(uniqueStationId);
        if (station == null) {
            station = new PollingStation(uniqueStationId, stationName, postalCode);
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

        // Municipality ID prefix with election ID
        String rawMunicipalityId = stationId.substring(0, 4);
        String municipalityId = election.getId() + "-" + rawMunicipalityId;
        String uniqueStationId = election.getId() + "-" + stationId;

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) return;

        // Find or create SB
        PollingStation station = municipality.getPollingStationById(uniqueStationId);
        if (station == null) {
            station = new PollingStation(uniqueStationId, stationName, postalCode);
            municipality.addPollingStation(station);
        }
    }
}
