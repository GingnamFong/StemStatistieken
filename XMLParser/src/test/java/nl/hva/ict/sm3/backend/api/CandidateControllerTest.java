package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.service.CandidateListService;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests voor CandidateController.
 * 
 * Test categorieën:
 * - Happy flows: normale, verwachte input
 * - Invalid input: verkeerde parameters, foutieve waarden, of ontbrekende data
 * - Foutafhandeling: werpt de code de juiste exception, of geeft de API de juiste statuscode terug?
 * - Business rules: zijn alle regels en validaties correct geïmplementeerd
 */
@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @Mock
    private DutchElectionService electionService;

    @Mock
    private CandidateListService candidateListService;

    // Dependency injection
    @InjectMocks
    private CandidateController candidateController;

    // HTTP simulate
    private MockMvc mockMvc;

    private Election testElection;
    private List<Candidate> testCandidates;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(candidateController).build();
        
        // Test data
        testElection = new Election("TK2023");
        testCandidates = new ArrayList<>();
        testCandidates.add(new Candidate("1-1", "Dilan", "Yeşilgöz", "D.", "Amsterdam", "1", "VVD", 1));
        testCandidates.add(new Candidate("1-2", "Rob", "Jetten", "R.A.A.", "Utrecht", "2", "D66", 2));
        testElection.getCandidates().addAll(testCandidates);
    }


    // HAPPY FLOWS - normale, verwachte input


    @Test
    void testLoadCandidateLists_HappyFlow_WithFolderName() throws Exception {
        // Arrange: Election bestaat met cache
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);

        // Act & Assert
        mockMvc.perform(post("/elections/TK2023/candidates/lists")
                        .param("folderName", "TK2023"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("TK2023"))
                .andExpect(jsonPath("$.candidates").isArray())
                .andExpect(jsonPath("$.candidates.length()").value(2));

        // Verify: 1x aangeroepen electionService, candidateListService leeg
        verify(electionService, times(1)).getElectionById("TK2023");
        verify(candidateListService, never()).loadCandidateLists(any(Election.class), anyString());
    }

    @Test
    void testGetCandidateById_HappyFlow() throws Exception {
        // Arrange: Election bestaat met cache
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);

        // Act & Assert: 1ste candidate teruggeven
        mockMvc.perform(get("/elections/TK2023/candidates/1-1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1-1"))
                .andExpect(jsonPath("$.firstName").value("Dilan"))
                .andExpect(jsonPath("$.lastName").value("Yeşilgöz"));

        // Verify
        verify(electionService, times(1)).getElectionById("TK2023");
        verify(candidateListService, never()).loadCandidateLists(any(Election.class), anyString());
    }


    // INVALID INPUT - verkeerde parameters, foutieve waarden, of ontbrekende data


    @Test
    void testLoadCandidateLists_ElectionNotFoundInCache() throws Exception {
        // Arrange: election bestaat niet in cache en wordt niet gecached
        when(electionService.getElectionById("TK2023")).thenReturn(null).thenReturn(null);
        
        // Doet niets en cache ook niet
        doNothing().when(candidateListService).loadCandidateLists(any(Election.class), anyString());

        // Act & Assert: controller geeft nieuwe election
        mockMvc.perform(post("/elections/TK2023/candidates/lists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("TK2023"));

        // Verify
        verify(electionService, times(2)).getElectionById("TK2023");
        verify(candidateListService, times(1)).loadCandidateLists(any(Election.class), anyString());
    }

    @Test
    void testGetCandidateById_CandidateNotFound() throws Exception {
        // Arrange: election bestaat, geen candidate
        when(electionService.getElectionById("TK2023")).thenReturn(testElection);

        // Act & Assert
        mockMvc.perform(get("/elections/TK2023/candidates/non-existent"))
                .andExpect(status().isNotFound());

        // Verify
        verify(electionService, times(1)).getElectionById("TK2023");
    }


    // FOUTAFHANDELING - exceptions en error handling


    @Test
    void testLoadCandidateLists_ServiceThrowsException() throws Exception {
        // Arrange: exception Database error
        when(electionService.getElectionById("TK2023"))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert: exception wordt doorgegeven
        try {
            mockMvc.perform(post("/elections/TK2023/candidates/lists"));
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

    @Test
    void testGetCandidateById_ElectionNotInCache() throws Exception {
        // Arrange: election niet in cache
        when(electionService.getElectionById("TK2023")).thenReturn(null);

        // fill election-object met testCandidates
        doAnswer(invocation -> {
            Election election = invocation.getArgument(0);
            election.getCandidates().addAll(testCandidates);
            return null;
        }).when(candidateListService).loadCandidateLists(any(Election.class), anyString());

        // Act & Assert: laden en candidate teruggeven
        mockMvc.perform(get("/elections/TK2023/candidates/1-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1-1"));

        // Verify
        verify(candidateListService, times(1)).loadCandidateLists(any(Election.class), eq("TK2023"));
        verify(electionService, times(1)).getElectionById("TK2023");
    }


    // BUSINESS RULES - regels en validaties


    @Test
    void testLoadCandidateLists_WithoutFolderNameUsesElectionId() throws Exception {
        // Arrange: moet electionId gebruiken
        when(electionService.getElectionById("TK2023"))
                .thenReturn(null)
                .thenReturn(testElection);
        doNothing().when(candidateListService).loadCandidateLists(any(Election.class), anyString());

        // Act & Assert
        mockMvc.perform(post("/elections/TK2023/candidates/lists"))
                .andExpect(status().isOk());

        // Verify: candidateListService met electionId
        verify(candidateListService, times(1)).loadCandidateLists(any(Election.class), eq("TK2023"));
    }

    @Test
    void testLoadCandidateLists_ReturnsCachedElectionAfterLoading() throws Exception {
        // Arrange: election niet in cache, wordt geladen en gecached
        when(electionService.getElectionById("TK2023"))
                .thenReturn(null)
                .thenReturn(testElection);
        doNothing().when(candidateListService).loadCandidateLists(any(Election.class), anyString());

        // Act & Assert: moet cached election teruggeven
        mockMvc.perform(post("/elections/TK2023/candidates/lists")
                        .param("folderName", "TK2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidates.length()").value(2));

        // Verify: tweede run geeft cached version terug
        verify(electionService, times(2)).getElectionById("TK2023");
    }
}
