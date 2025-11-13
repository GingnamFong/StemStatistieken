package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.utils.xml.VotesTransformer;

import java.util.Map;

/**
 * Handles BOTH municipality totals (aggregated) and polling station results (non-aggregated).
 * This transformer receives all vote-related events from EMLHandler.
 *
 * aggregated = true  → municipality-level totals
 * aggregated = false → polling-station (stembureau) level
 */
public class DutchMunicipalityVotesTransformer implements VotesTransformer {

    private final Election election;

    // Tracks which polling station we are currently parsing
    private PollingStation currentStation = null;

    public DutchMunicipalityVotesTransformer(Election election) {
        this.election = election;
    }


    // ---------------------------------------------------------
    //  METADATA HANDLER
    //  Triggered for <REPORTING_UNIT_VOTES> end tag
    // ---------------------------------------------------------
    @Override
    public void registerMetadata(boolean aggregated, Map<String, String> data) {

        if (aggregated) {
            currentStation = null;
            return;
        }

        String municipalityId = data.get("AuthorityIdentifier-Id");
        String stationId = data.get("ReportingUnitIdentifier-Id");
        String fullText = data.get("ReportingUnitIdentifier");

        if (municipalityId == null || stationId == null) {
            System.err.println("⚠ Missing municipality or station ID");
            return;
        }

        Municipality municipality = election.getMunicipalityById(municipalityId);
        if (municipality == null) {
            System.err.println("⚠ Municipality ID " + municipalityId + " not found for station " + fullText);
            return;
        }

        // --------------------------------------------
        // Parse station name + postal code
        // --------------------------------------------
        String stationName = fullText;
        String postalCode = null;

        if (fullText != null && fullText.contains("postcode:")) {
            int start = fullText.indexOf("postcode:") + 9;
            int end = fullText.indexOf(")", start);

            if (end > start) {
                postalCode = fullText.substring(start, end).trim();
            }

            stationName = fullText.substring(0, fullText.indexOf("(")).trim();
        }

        // Create polling station
        currentStation = new PollingStation(stationId, stationName, postalCode);

        municipality.addPollingStation(currentStation);
    }


    // ---------------------------------------------------------
    //  PARTY VOTES
    // ---------------------------------------------------------
    @Override
    public void registerPartyVotes(boolean aggregated, Map<String, String> data) {

        String partyId = data.get("AffiliationIdentifier-Id");
        String partyName = data.get("RegisteredName");
        String votesStr = data.get("ValidVotes");

        if (partyId == null || votesStr == null) {
            return;
        }

        int votes = parseIntSafe(votesStr);

        if (aggregated) {
            // MUNICIPALITY TOTAL LEVEL
            String municipalityId = data.get("AuthorityIdentifier-Id");
            String municipalityName = data.get("AuthorityIdentifier");

            if (municipalityId == null) return;

            // Find constituency & municipality
            for (Constituency c : election.getConstituencies()) {
                Municipality m = c.getMunicipalityById(municipalityId);

                if (m != null) {
                    m.addVotesForParty(partyId,
                            partyName != null ? partyName : "Unknown",
                            votes
                    );
                    return;
                }
            }

            System.err.println("⚠ Municipality not found: " + municipalityId);
        } else {
            // POLLING STATION LEVEL
            if (currentStation != null) {
                currentStation.addResult(new VoteResult("party", partyId, votes));
            }
        }
    }


    // ---------------------------------------------------------
    //  CANDIDATE VOTES
    // ---------------------------------------------------------
    @Override
    public void registerCandidateVotes(boolean aggregated, Map<String, String> data) {

        if (aggregated || currentStation == null) return;

        String candidateId = data.get("CandidateIdentifier-Id");
        String votesStr = data.get("ValidVotes");

        if (candidateId == null || votesStr == null) {
            return;
        }

        int votes = parseIntSafe(votesStr);

        currentStation.addResult(new VoteResult("candidate", candidateId, votes));
    }


    // ---------------------------------------------------------
    // Utilities
    // ---------------------------------------------------------
    private int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.err.println("⚠ Invalid number: " + s);
            return 0;
        }
    }
}
