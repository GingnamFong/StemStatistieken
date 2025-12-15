package nl.hva.ict.sm3.backend.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating stemwijzer matches.
 */
@Service
public class StemwijzerService {

    // Party information: partyId -> (name, color)
    private static final Map<String, PartyInfo> PARTIES = createParties();

    private static Map<String, PartyInfo> createParties() {
        Map<String, PartyInfo> parties = new HashMap<>();
        parties.put("VVD", new PartyInfo("VVD", "#014A7F"));
        parties.put("D66", new PartyInfo("D66", "#00A1CD"));
        parties.put("PVV", new PartyInfo("PVV", "#0079D3"));
        parties.put("CDA", new PartyInfo("CDA", "#009639"));
        parties.put("SP", new PartyInfo("SP", "#E30613"));
        parties.put("PvdA", new PartyInfo("PvdA", "#E30613"));
        parties.put("GL", new PartyInfo("GroenLinks", "#00A651"));
        parties.put("PvdD", new PartyInfo("Partij voor de Dieren", "#8BC34A"));
        parties.put("CU", new PartyInfo("ChristenUnie", "#00A1CD"));
        parties.put("SGP", new PartyInfo("SGP", "#003E7E"));
        parties.put("DENK", new PartyInfo("DENK", "#FFD700"));
        parties.put("FVD", new PartyInfo("Forum voor Democratie", "#000000"));
        parties.put("JA21", new PartyInfo("JA21", "#FF6B00"));
        parties.put("Volt", new PartyInfo("Volt", "#502379"));
        parties.put("BBB", new PartyInfo("BoerBurgerBeweging", "#00A651"));
        parties.put("NSC", new PartyInfo("NSC", "#1E3A8A"));
        return Collections.unmodifiableMap(parties);
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
     */
    public List<Map<String, Object>> calculateMatches(Map<Integer, String> answers) {
        if (answers == null || answers.isEmpty()) {
            return new ArrayList<>();
        }

        // Calculate scores for each party using streams
        return PARTIES.keySet().stream()
                .map(partyId -> {
                    PartyScore score = calculatePartyScore(partyId, answers);
                    PartyInfo partyInfo = PARTIES.get(partyId);
                    
                    if (score.totalQuestions == 0) {
                        return null; // Skip parties with no valid answers
                    }
                    
                    int matchPercentage = Math.round((float) score.matchingAnswers / score.totalQuestions * 100);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("partyId", partyId);
                    result.put("partyName", partyInfo.name);
                    result.put("partyColor", partyInfo.color);
                    result.put("matchPercentage", matchPercentage);
                    
                    return result;
                })
                .filter(Objects::nonNull) // Filter out null results
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
