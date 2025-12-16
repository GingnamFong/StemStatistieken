package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.Party;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating stemwijzer matches.
 */
@Service
public class StemwijzerService {

    private final DutchElectionService electionService;
    private static final String ELECTION_ID = "TK2025";

    // Color mapping: partyId (without election prefix) -> color
    private static final Map<String, String> PARTY_COLORS = createPartyColors();

    private static Map<String, String> createPartyColors() {
        Map<String, String> colors = new HashMap<>();
        colors.put("VVD", "#014A7F");
        colors.put("D66", "#00A1CD");
        colors.put("PVV", "#0079D3");
        colors.put("CDA", "#009639");
        colors.put("SP", "#E30613");
        colors.put("PvdA", "#E30613");
        colors.put("GL", "#00A651");
        colors.put("PvdD", "#8BC34A");
        colors.put("CU", "#00A1CD");
        colors.put("SGP", "#003E7E");
        colors.put("DENK", "#FFD700");
        colors.put("FVD", "#000000");
        colors.put("JA21", "#FF6B00");
        colors.put("Volt", "#502379");
        colors.put("BBB", "#00A651");
        colors.put("NSC", "#1E3A8A");
        return Collections.unmodifiableMap(colors);
    }

    public StemwijzerService(DutchElectionService electionService) {
        this.electionService = electionService;
    }

    /**
     * Gets party information from XML data (TK2025) combined with hardcoded colors.
     * Only includes parties that have positions defined in PARTY_POSITIONS.
     *
     * @return map of partyId -> (name, color)
     */
    private Map<String, PartyInfo> getParties() {
        Map<String, PartyInfo> parties = new HashMap<>();
        
        // Get all party IDs that have positions defined
        Set<String> knownPartyIds = PARTY_POSITIONS.values().stream()
                .flatMap(positions -> positions.keySet().stream())
                .collect(Collectors.toSet());
        
        try {
            Election election = electionService.getElectionById(ELECTION_ID);
            if (election == null) {
                throw new IllegalStateException("TK2025 election data niet gevonden. Zorg ervoor dat de verkiezingsdata is geladen in de database.");
            }
            
            // Force initialization of parties collection
            List<Party> partyList = election.getParties();
            if (partyList == null || partyList.isEmpty()) {
                throw new IllegalStateException("Geen partijen gevonden in TK2025 verkiezingsdata. Zorg ervoor dat de verkiezingsdata volledig is geladen.");
            }
            
            // Create a mapping of party names to IDs for better matching
            // Include actual names from XML
            Map<String, String> nameToIdMap = new HashMap<>();
            nameToIdMap.put("VVD", "VVD");
            nameToIdMap.put("D66", "D66");
            nameToIdMap.put("D 66", "D66");
            nameToIdMap.put("PVV", "PVV");
            nameToIdMap.put("Partij voor de Vrijheid", "PVV");
            nameToIdMap.put("PVV (Partij voor de Vrijheid)", "PVV");
            nameToIdMap.put("CDA", "CDA");
            nameToIdMap.put("SP", "SP");
            nameToIdMap.put("Socialistische Partij", "SP");
            nameToIdMap.put("SP (Socialistische Partij)", "SP");
            nameToIdMap.put("PvdA", "PvdA");
            nameToIdMap.put("Partij van de Arbeid", "PvdA");
            nameToIdMap.put("GROENLINKS / Partij van de Arbeid (PvdA)", "PvdA");
            nameToIdMap.put("GL", "GL");
            nameToIdMap.put("GroenLinks", "GL");
            nameToIdMap.put("GROENLINKS", "GL");
            nameToIdMap.put("PvdD", "PvdD");
            nameToIdMap.put("Partij voor de Dieren", "PvdD");
            nameToIdMap.put("CU", "CU");
            nameToIdMap.put("ChristenUnie", "CU");
            nameToIdMap.put("SGP", "SGP");
            nameToIdMap.put("Staatkundig Gereformeerde Partij", "SGP");
            nameToIdMap.put("Staatkundig Gereformeerde Partij (SGP)", "SGP");
            nameToIdMap.put("DENK", "DENK");
            nameToIdMap.put("FVD", "FVD");
            nameToIdMap.put("Forum voor Democratie", "FVD");
            nameToIdMap.put("Forum voor Democratie (FVD)", "FVD");
            nameToIdMap.put("JA21", "JA21");
            nameToIdMap.put("Volt", "Volt");
            nameToIdMap.put("BBB", "BBB");
            nameToIdMap.put("BoerBurgerBeweging", "BBB");
            nameToIdMap.put("NSC", "NSC");
            nameToIdMap.put("Nieuw Sociaal Contract", "NSC");
            nameToIdMap.put("Nieuw Sociaal Contract (NSC)", "NSC");
            
            // Get seat allocations - try from map first, then from National records
            Map<String, Integer> seatAllocations = election.getSeatAllocations();
            if (seatAllocations == null || seatAllocations.isEmpty()) {
                // Try to get seats from National records
                seatAllocations = new HashMap<>();
                for (National national : election.getNationalVotes()) {
                    if (national != null && national.getNumberOfSeats() > 0 && national.getPartyId() != null) {
                        seatAllocations.put(national.getPartyId(), national.getNumberOfSeats());
                    }
                }
            }
            
            boolean hasSeatData = !seatAllocations.isEmpty();
            
            for (Party party : partyList) {
                String partyId = party.getId();
                String partyName = party.getName();
                
                if (partyName == null) {
                    continue;
                }
                
                // Check if party has 1+ seats (if seat data is available)
                if (hasSeatData) {
                    int seats = seatAllocations.getOrDefault(partyId, 0);
                    if (seats < 1) {
                        continue;
                    }
                }
                // If no seat data available, include all parties (they will be filtered by positions)
                
                // Match by name (since IDs are numbers, not letter codes)
                String matchedId = null;
                String normalizedName = partyName.trim();
                
                // Direct match
                matchedId = nameToIdMap.get(normalizedName);
                
                // Try case-insensitive match
                if (matchedId == null) {
                    for (Map.Entry<String, String> entry : nameToIdMap.entrySet()) {
                        if (normalizedName.equalsIgnoreCase(entry.getKey())) {
                            matchedId = entry.getValue();
                            break;
                        }
                    }
                }
                
                // Try partial match (contains)
                if (matchedId == null) {
                    for (Map.Entry<String, String> entry : nameToIdMap.entrySet()) {
                        if (normalizedName.contains(entry.getKey()) || entry.getKey().contains(normalizedName)) {
                            matchedId = entry.getValue();
                            break;
                        }
                    }
                }
                
                // Try matching key parts of the name (case-insensitive)
                if (matchedId == null) {
                    String upperName = normalizedName.toUpperCase();
                    // Check for specific patterns from XML
                    if (upperName.contains("VVD") && !upperName.contains("PARTIJ")) matchedId = "VVD";
                    else if (upperName.contains("D66") || upperName.contains("D 66")) matchedId = "D66";
                    else if (upperName.contains("PVV") || (upperName.contains("PARTIJ") && upperName.contains("VRIJHEID"))) matchedId = "PVV";
                    else if (upperName.contains("CDA")) matchedId = "CDA";
                    else if (upperName.contains("SP") && upperName.contains("SOCIALIST")) matchedId = "SP";
                    else if (upperName.contains("GROENLINKS") && upperName.contains("ARBEID")) matchedId = "PvdA"; // "GROENLINKS / Partij van de Arbeid (PvdA)"
                    else if (upperName.contains("GROENLINKS") || (upperName.contains("GL") && !upperName.contains("PARTIJ") && !upperName.contains("ARBEID"))) matchedId = "GL";
                    else if (upperName.contains("PARTIJ") && upperName.contains("DIEREN")) matchedId = "PvdD";
                    else if (upperName.contains("CHRISTENUNIE") || (upperName.contains("CU") && !upperName.contains("PARTIJ"))) matchedId = "CU";
                    else if (upperName.contains("STAATKUNDIG") || (upperName.contains("SGP") && !upperName.contains("PARTIJ"))) matchedId = "SGP";
                    else if (upperName.contains("DENK")) matchedId = "DENK";
                    else if (upperName.contains("FORUM") || (upperName.contains("FVD") && !upperName.contains("PARTIJ"))) matchedId = "FVD";
                    else if (upperName.contains("JA21")) matchedId = "JA21";
                    else if (upperName.contains("VOLT")) matchedId = "Volt";
                    else if (upperName.contains("BOERBURGER") || upperName.contains("BBB")) matchedId = "BBB";
                    else if (upperName.contains("NIEUW SOCIAAL") || (upperName.contains("NSC") && !upperName.contains("PARTIJ"))) matchedId = "NSC";
                }
                
                // Only include parties that have positions defined
                if (matchedId != null && knownPartyIds.contains(matchedId)) {
                    // Get color from mapping, default to gray if not found
                    String color = PARTY_COLORS.getOrDefault(matchedId, "#64748b");
                    parties.put(matchedId, new PartyInfo(partyName, color));
                }
            }
            
            if (parties.isEmpty()) {
                // Log available parties for debugging
                System.err.println("ERROR: No parties matched. Available parties:");
                for (Party party : partyList) {
                    int seats = hasSeatData ? election.getSeatsForParty(party.getId()) : -1;
                    System.err.println("  - " + party.getId() + ": " + party.getName() + (hasSeatData ? " (" + seats + " seats)" : ""));
                }
                throw new IllegalStateException("Geen partijen met posities gevonden in TK2025 verkiezingsdata.");
            }
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Fout bij het laden van TK2025 verkiezingsdata: " + e.getMessage(), e);
        }
        
        return parties;
    }
    
    // Party positions per question: questionId -> partyId -> position (eens/oneens)
    private static final Map<Integer, Map<String, String>> PARTY_POSITIONS = createPartyPositions();

    private static Map<Integer, Map<String, String>> createPartyPositions() {
        Map<Integer, Map<String, String>> positions = new HashMap<>();
        
        // Question 1: Klimaat
        Map<String, String> q1 = new HashMap<>();
        q1.put("VVD", "eens"); q1.put("D66", "eens"); q1.put("PVV", "oneens"); q1.put("CDA", "eens");
        q1.put("SP", "eens"); q1.put("PvdA", "eens"); q1.put("GL", "eens"); q1.put("PvdD", "eens");
        q1.put("CU", "eens"); q1.put("SGP", "eens"); q1.put("DENK", "eens"); q1.put("FVD", "oneens");
        q1.put("JA21", "oneens"); q1.put("Volt", "eens"); q1.put("BBB", "oneens"); q1.put("NSC", "eens");
        positions.put(1, Collections.unmodifiableMap(q1));
        
        // Question 2: Immigratie
        Map<String, String> q2 = new HashMap<>();
        q2.put("VVD", "eens"); q2.put("D66", "oneens"); q2.put("PVV", "eens"); q2.put("CDA", "eens");
        q2.put("SP", "oneens"); q2.put("PvdA", "oneens"); q2.put("GL", "oneens"); q2.put("PvdD", "oneens");
        q2.put("CU", "eens"); q2.put("SGP", "eens"); q2.put("DENK", "oneens"); q2.put("FVD", "eens");
        q2.put("JA21", "eens"); q2.put("Volt", "oneens"); q2.put("BBB", "eens"); q2.put("NSC", "eens");
        positions.put(2, Collections.unmodifiableMap(q2));
        
        // Question 3: Zorg
        Map<String, String> q3 = new HashMap<>();
        q3.put("VVD", "eens"); q3.put("D66", "oneens"); q3.put("PVV", "oneens"); q3.put("CDA", "eens");
        q3.put("SP", "oneens"); q3.put("PvdA", "oneens"); q3.put("GL", "oneens"); q3.put("PvdD", "oneens");
        q3.put("CU", "oneens"); q3.put("SGP", "eens"); q3.put("DENK", "oneens"); q3.put("FVD", "eens");
        q3.put("JA21", "eens"); q3.put("Volt", "oneens"); q3.put("BBB", "oneens"); q3.put("NSC", "eens");
        positions.put(3, Collections.unmodifiableMap(q3));
        
        // Question 4: Woningmarkt
        Map<String, String> q4 = new HashMap<>();
        q4.put("VVD", "eens"); q4.put("D66", "eens"); q4.put("PVV", "eens"); q4.put("CDA", "eens");
        q4.put("SP", "eens"); q4.put("PvdA", "eens"); q4.put("GL", "oneens"); q4.put("PvdD", "oneens");
        q4.put("CU", "eens"); q4.put("SGP", "eens"); q4.put("DENK", "eens"); q4.put("FVD", "eens");
        q4.put("JA21", "eens"); q4.put("Volt", "eens"); q4.put("BBB", "oneens"); q4.put("NSC", "eens");
        positions.put(4, Collections.unmodifiableMap(q4));
        
        // Question 5: Minimumloon
        Map<String, String> q5 = new HashMap<>();
        q5.put("VVD", "oneens"); q5.put("D66", "eens"); q5.put("PVV", "eens"); q5.put("CDA", "eens");
        q5.put("SP", "eens"); q5.put("PvdA", "eens"); q5.put("GL", "eens"); q5.put("PvdD", "eens");
        q5.put("CU", "eens"); q5.put("SGP", "eens"); q5.put("DENK", "eens"); q5.put("FVD", "oneens");
        q5.put("JA21", "oneens"); q5.put("Volt", "eens"); q5.put("BBB", "eens"); q5.put("NSC", "eens");
        positions.put(5, Collections.unmodifiableMap(q5));
        
        // Question 6: Europa
        Map<String, String> q6 = new HashMap<>();
        q6.put("VVD", "eens"); q6.put("D66", "eens"); q6.put("PVV", "oneens"); q6.put("CDA", "eens");
        q6.put("SP", "eens"); q6.put("PvdA", "eens"); q6.put("GL", "eens"); q6.put("PvdD", "eens");
        q6.put("CU", "eens"); q6.put("SGP", "oneens"); q6.put("DENK", "eens"); q6.put("FVD", "oneens");
        q6.put("JA21", "oneens"); q6.put("Volt", "eens"); q6.put("BBB", "eens"); q6.put("NSC", "eens");
        positions.put(6, Collections.unmodifiableMap(q6));
        
        // Question 7: Onderwijs
        Map<String, String> q7 = new HashMap<>();
        q7.put("VVD", "oneens"); q7.put("D66", "eens"); q7.put("PVV", "oneens"); q7.put("CDA", "eens");
        q7.put("SP", "eens"); q7.put("PvdA", "eens"); q7.put("GL", "eens"); q7.put("PvdD", "eens");
        q7.put("CU", "eens"); q7.put("SGP", "eens"); q7.put("DENK", "eens"); q7.put("FVD", "oneens");
        q7.put("JA21", "oneens"); q7.put("Volt", "eens"); q7.put("BBB", "eens"); q7.put("NSC", "eens");
        positions.put(7, Collections.unmodifiableMap(q7));
        
        // Question 8: Bedrijfsbelastingen
        Map<String, String> q8 = new HashMap<>();
        q8.put("VVD", "eens"); q8.put("D66", "oneens"); q8.put("PVV", "eens"); q8.put("CDA", "eens");
        q8.put("SP", "oneens"); q8.put("PvdA", "oneens"); q8.put("GL", "oneens"); q8.put("PvdD", "oneens");
        q8.put("CU", "eens"); q8.put("SGP", "eens"); q8.put("DENK", "oneens"); q8.put("FVD", "eens");
        q8.put("JA21", "eens"); q8.put("Volt", "oneens"); q8.put("BBB", "eens"); q8.put("NSC", "eens");
        positions.put(8, Collections.unmodifiableMap(q8));
        
        // Question 9: Biodiversiteit
        Map<String, String> q9 = new HashMap<>();
        q9.put("VVD", "eens"); q9.put("D66", "eens"); q9.put("PVV", "oneens"); q9.put("CDA", "eens");
        q9.put("SP", "eens"); q9.put("PvdA", "eens"); q9.put("GL", "eens"); q9.put("PvdD", "eens");
        q9.put("CU", "eens"); q9.put("SGP", "eens"); q9.put("DENK", "eens"); q9.put("FVD", "oneens");
        q9.put("JA21", "oneens"); q9.put("Volt", "eens"); q9.put("BBB", "oneens"); q9.put("NSC", "eens");
        positions.put(9, Collections.unmodifiableMap(q9));
        
        // Question 10: Defensie
        Map<String, String> q10 = new HashMap<>();
        q10.put("VVD", "eens"); q10.put("D66", "eens"); q10.put("PVV", "eens"); q10.put("CDA", "eens");
        q10.put("SP", "oneens"); q10.put("PvdA", "eens"); q10.put("GL", "oneens"); q10.put("PvdD", "oneens");
        q10.put("CU", "eens"); q10.put("SGP", "eens"); q10.put("DENK", "oneens"); q10.put("FVD", "eens");
        q10.put("JA21", "eens"); q10.put("Volt", "eens"); q10.put("BBB", "eens"); q10.put("NSC", "eens");
        positions.put(10, Collections.unmodifiableMap(q10));
        
        return Collections.unmodifiableMap(positions);
    }

    /**
     * Calculates match scores between user answers and party positions.
     *
     * @param answers map of questionId to answer
     * @return list of match results sorted by percentage
     * @throws IllegalStateException if TK2025 election data is not available
     */
    public List<Map<String, Object>> calculateMatches(Map<Integer, String> answers) {
        if (answers == null || answers.isEmpty()) {
            return new ArrayList<>();
        }

        // Get parties from XML data
        Map<String, PartyInfo> parties;
        try {
            parties = getParties();
        } catch (IllegalStateException e) {
            throw new IllegalStateException("TK2025 election data not available. Please ensure the election is loaded in the database.", e);
        }
        
        // Calculate scores for each party using streams
        // Only process parties that have positions defined
        return parties.keySet().stream()
                .map(partyId -> {
                    PartyScore score = calculatePartyScore(partyId, answers);
                    PartyInfo partyInfo = parties.get(partyId);
                    
                    // All parties in the map should have positions, but double-check
                    if (score.totalQuestions == 0) {
                        return null;
                    }
                    
                    int matchPercentage = Math.round((float) score.matchingAnswers / score.totalQuestions * 100);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("partyId", partyId);
                    result.put("partyName", partyInfo.name);
                    result.put("partyColor", partyInfo.color);
                    result.put("matchPercentage", matchPercentage);
                    
                    return result;
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> {
                    // Sort by match percentage descending, then by party name alphabetically
                    int percentageCompare = Integer.compare(
                            (Integer) b.get("matchPercentage"),
                            (Integer) a.get("matchPercentage")
                    );
                    if (percentageCompare != 0) {
                        return percentageCompare;
                    }
                    // Use compareTo for alphabetical sorting
                    return ((String) a.get("partyName")).compareTo((String) b.get("partyName"));
                })
                .limit(10) // Top 10 matches
                .collect(Collectors.toList());
    }

    /**
     * Calculates the score for a single party.
     *
     * @param partyId party ID
     * @param answers user answers
     * @return party score
     */
    private PartyScore calculatePartyScore(String partyId, Map<Integer, String> answers) {
        int matchingAnswers = 0;
        int totalQuestions = 0;

        for (Map.Entry<Integer, String> entry : answers.entrySet()) {
            Integer questionId = entry.getKey();
            String userAnswer = entry.getValue();

            // Skip "geen-mening" answers using filtering
            if ("geen-mening".equals(userAnswer)) {
                continue;
            }

            totalQuestions++;
            Map<String, String> questionPositions = PARTY_POSITIONS.get(questionId);
            
            if (questionPositions != null) {
                String partyPosition = questionPositions.get(partyId);
                if (partyPosition != null && partyPosition.equals(userAnswer)) {
                    matchingAnswers++;
                }
                // If party has no position defined, it doesn't match (counts as 0% for that question)
            } else {
                // If question has no positions defined, skip it
                totalQuestions--;
            }
        }

        return new PartyScore(partyId, matchingAnswers, totalQuestions);
    }

    // Helper classes
    private static class PartyInfo {
        final String name;
        final String color;

        PartyInfo(String name, String color) {
            this.name = name;
            this.color = color;
        }
    }

    private static class PartyScore {
        final String partyId;
        final int matchingAnswers;
        final int totalQuestions;

        PartyScore(String partyId, int matchingAnswers, int totalQuestions) {
            this.partyId = partyId;
            this.matchingAnswers = matchingAnswers;
            this.totalQuestions = totalQuestions;
        }
    }
}
