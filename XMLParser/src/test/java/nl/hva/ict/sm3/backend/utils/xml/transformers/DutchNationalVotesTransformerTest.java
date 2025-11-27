package nl.hva.ict.sm3.backend.utils.xml.transformers;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.AFFILIATION_IDENTIFIER;
import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.CANDIDATE_IDENTIFIER_ID;
import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.CANDIDATE_IDENTIFIER_SHORT_CODE;
import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.COUNT;
import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.ID;
import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.VALID_VOTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DutchNationalVotesTransformerTest {

    private Election election;
    private Candidate candidate;
    private DutchNationalVotesTransformer transformer;

    @BeforeEach
    void setUp() {
        election = new Election("TK2023");
        candidate = new Candidate("P1-1", "Test", "Tester", "T.", "Amsterdam", "P1", "Test Party", 1);
        election.addCandidate(candidate);
        transformer = new DutchNationalVotesTransformer(election);
    }

    @Test
    void registerCandidateVotes_updatesVotesAndShortCodeWhenAggregated() {
        Map<String, String> data = new HashMap<>();
        data.put(AFFILIATION_IDENTIFIER + "-" + ID, "P1");
        data.put(CANDIDATE_IDENTIFIER_ID, "1");
        data.put(CANDIDATE_IDENTIFIER_SHORT_CODE, "TesterT");
        data.put(VALID_VOTES, "1234");

        transformer.registerCandidateVotes(true, data);

        assertEquals(1234, candidate.getVotes());
        assertEquals("TesterT", candidate.getShortCode());
    }

    @Test
    void registerCandidateVotes_skipsNonAggregatedData() {
        Map<String, String> data = new HashMap<>();
        data.put(AFFILIATION_IDENTIFIER + "-" + ID, "P1");
        data.put(CANDIDATE_IDENTIFIER_ID, "1");
        data.put(VALID_VOTES, "999");

        transformer.registerCandidateVotes(false, data);

        assertEquals(0, candidate.getVotes());
        assertNull(candidate.getShortCode());
    }

    @Test
    void registerCandidateVotes_fallsBackToCountAndShortCodeLookup() {
        Map<String, String> data = new HashMap<>();
        data.put(CANDIDATE_IDENTIFIER_SHORT_CODE, "TesterT");
        data.put(COUNT, "77");

        transformer.registerCandidateVotes(true, data);

        assertEquals(77, candidate.getVotes());
        assertEquals("TesterT", candidate.getShortCode());
    }
}