package nl.hva.ict.sm3.backend.utils.xml;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.utils.xml.transformers.DutchCandidateTransformer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.hva.ict.sm3.backend.utils.xml.TagAndAttributeNames.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CandidateListMockitoTest {

    @Mock
    private Election election;

    private DutchCandidateTransformer transformer;
    private List<Candidate> candidates;

    @BeforeEach
    void setUp() {
        transformer = new DutchCandidateTransformer(election);
        candidates = new ArrayList<>();
        when(election.getCandidates()).thenReturn(candidates);
    }

    @Test
    void testRegisterCandidate_HappyFlow_AddsCandidateToElection() {

        Map<String, String> electionData = createCandidateElectionData();

        transformer.registerCandidate(electionData);

        //Capture agurment verify 1x to verify data
        ArgumentCaptor<Candidate> candidateCaptor = ArgumentCaptor.forClass(Candidate.class);
        verify(election, times(1)).addCandidate(candidateCaptor.capture());
        
        Candidate capturedCandidate = candidateCaptor.getValue();
        assertEquals("1-1", capturedCandidate.getId());
        assertEquals("Dilan", capturedCandidate.getFirstName());
        assertEquals("Yeşilgöz", capturedCandidate.getLastName());
        assertEquals("D.", capturedCandidate.getInitials());
        assertEquals("Amsterdam", capturedCandidate.getResidence());
        assertEquals("1", capturedCandidate.getPartyId());
        assertEquals("VVD", capturedCandidate.getPartyName());
    }

    // Create candidate election data
    private Map<String, String> createCandidateElectionData() {
        Map<String, String> electionData = new HashMap<>();
        electionData.put(CANDIDATE_IDENTIFIER_ID, "1");
        electionData.put(String.format("%s-%s", NAME_LINE, "Initials"), "D.");
        electionData.put(NAME_LINE, "D.");
        electionData.put(FIRST_NAME, "Dilan");
        electionData.put(LAST_NAME, "Yeşilgöz");
        electionData.put(LOCALITY_NAME, "Amsterdam");
        electionData.put(AFFILIATION_IDENTIFIER + "-Id", "1");
        electionData.put(REGISTERED_NAME, "VVD");
        return electionData;
    }
}
