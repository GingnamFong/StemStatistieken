package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Election;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests voor CandidateListService.
 * 
 * Test categorieën:
 * - Happy flows: normale, verwachte input
 * - Invalid input: verkeerde parameters, foutieve waarden, of ontbrekende data
 * - Foutafhandeling: werpt de code de juiste exception, of geeft de API de juiste statuscode terug?
 * - Business rules: zijn alle regels en validaties correct geïmplementeerd
 */
@ExtendWith(MockitoExtension.class)
class CandidateListServiceTest {

    // Mock DutchElectionService
    @Mock
    private DutchElectionService electionService;

    // Automatische dependency injection
    @InjectMocks
    private CandidateListService candidateListService;

    // Test data die we in meerdere tests gebruiken
    private Election testElection;
    private Election cachedElection;
    private List<Candidate> testCandidates;

    @BeforeEach
    void setUp() {
        // Maakt election
        testElection = new Election("TK2023");
        cachedElection = new Election("TK2023");
        
        // Maakt candidates
        testCandidates = new ArrayList<>();
        testCandidates.add(new Candidate("1-1", "Dilan", "Yeşilgöz", "D.", "Amsterdam", "1", "VVD", 1));
        testCandidates.add(new Candidate("1-2", "Rob", "Jetten", "R.A.A.", "Utrecht", "2", "D66", 2));
        testCandidates.add(new Candidate("1-3", "Geert", "Wilders", "G.", "Den Haag", "3", "PVV", 3));
    }


    // HAPPY FLOWS - normale, verwachte input


    @Test
    void testLoadCandidateLists_HappyFlow_CachedWithCandidates() {
        // Arrange: Cached candidates
        cachedElection.getCandidates().addAll(testCandidates);
        when(electionService.getElectionById("TK2023")).thenReturn(cachedElection);

        // Act
        candidateListService.loadCandidateLists(testElection, "TK2023");
        
        // Assert: verify candidates kopie
        verify(electionService, times(1)).getElectionById("TK2023");
        assertThat(testElection.getCandidates()).hasSize(3);
        assertThat(testElection.getCandidates())
                .extracting(Candidate::getId)
                .containsExactly("1-1", "1-2", "1-3");
        
        // Geen nieuwe cache
        verify(electionService, never()).cacheElection(anyString(), any(Election.class));
    }

    @Test
    void testLoadCandidateLists_HappyFlow_NotCached() {
        // Arrange: Parsen
        when(electionService.getElectionById("TK2023")).thenReturn(null);

        // Act
        candidateListService.loadCandidateLists(testElection, "TK2023");

        // Assert: getElectionById aangeroepen
        verify(electionService, times(1)).getElectionById("TK2023");
    }


    // INVALID INPUT - verkeerde parameters, foutieve waarden, of ontbrekende data


    @Test
    void testLoadCandidateLists_NullElection() {
        // Arrange: NullPointerException voor service

        // Act & Assert: NullPointerException wordt verwacht
        assertThatThrownBy(() -> candidateListService.loadCandidateLists(null, "TK2023"))
                .isInstanceOf(NullPointerException.class);
        
        // Verify: geen call bij direct een exception
        verify(electionService, never()).getElectionById(anyString());
    }

    @Test
    void testLoadCandidateLists_InvalidFolderName() {
        // Arrange: folder name die niet bestaat
        when(electionService.getElectionById("TK2023")).thenReturn(null);

        // Act: service vangt exceptions
        candidateListService.loadCandidateLists(testElection, "NonExistentFolder");

        // Assert: Parsing zal falen, vangt exception op cacheElection niet aangeroepen bij failure
        verify(electionService, times(1)).getElectionById("TK2023");
        assertThat(testElection.getCandidates()).isEmpty();
        verify(electionService, never()).cacheElection(anyString(), any(Election.class));
    }


    // FOUTAFHANDELING - exceptions en error handling


    @Test
    void testLoadCandidateLists_ServiceThrowsException() {
        // Arrange: electionService gooit een exception
        when(electionService.getElectionById("TK2023"))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert: exception moet worden gegooid
        assertThatThrownBy(() -> candidateListService.loadCandidateLists(testElection, "TK2023"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database connection failed");
    }

    @Test
    void testLoadCandidateLists_HandlesIOExceptionGracefully() {
        // Arrange: geen cache parsing fail
        when(electionService.getElectionById("TK2023")).thenReturn(null);

        // Act: IOException veroorzaken service vangt op
        candidateListService.loadCandidateLists(testElection, "NonExistentFolder12345");

        // Assert: geen candidates toegevoegd
        verify(electionService, times(1)).getElectionById("TK2023");
        assertThat(testElection.getCandidates()).isEmpty();
        // Cache failure
        verify(electionService, never()).cacheElection(anyString(), any(Election.class));
    }


    // BUSINESS RULES - regels en validaties


    @Test
    void testLoadCandidateLists_CachePreventsRedundantParsing() {
        // Arrange: cached election met candidates
        cachedElection.getCandidates().addAll(testCandidates);
        when(electionService.getElectionById("TK2023")).thenReturn(cachedElection);

        // Act: meerdere keren aanroepen
        candidateListService.loadCandidateLists(testElection, "TK2023");
        Election secondElection = new Election("TK2023");
        candidateListService.loadCandidateLists(secondElection, "TK2023");

        // Assert: elections hebben zelfde cache candidates
        assertThat(testElection.getCandidates()).hasSize(3);
        assertThat(secondElection.getCandidates()).hasSize(3);
        assertThat(secondElection.getCandidates())
                .extracting(Candidate::getId)
                .containsExactly("1-1", "1-2", "1-3");

        // Assert: getElectionById 2x aangeroepen
        verify(electionService, times(2)).getElectionById("TK2023");

        // Assert: cache nooit aangeroepen
        verify(electionService, never()).cacheElection(anyString(), any(Election.class));
    }

    @Test
    void testLoadCandidateLists_PreventsDuplicateCandidates() {
        // Arrange: candidates overlap
        Candidate candidate1 = new Candidate("1-1", "Dilan", "Yeşilgöz", "D.", "Amsterdam", "1", "VVD", 1);
        Candidate candidate2 = new Candidate("1-2", "Rob", "Jetten", "R.A.A.", "Utrecht", "2", "D66", 2);
        Candidate candidate3 = new Candidate("1-3", "Geert", "Wilders", "G.", "Den Haag", "3", "PVV", 3);

        //Exist
        testElection.addCandidate(candidate1);
        testElection.addCandidate(candidate2);
        
        cachedElection.getCandidates().add(candidate1);
        cachedElection.getCandidates().add(candidate2);
        cachedElection.getCandidates().add(candidate3); // Nieuw
        
        when(electionService.getElectionById("TK2023")).thenReturn(cachedElection);

        // Act
        candidateListService.loadCandidateLists(testElection, "TK2023");

        // Assert: alleen unieke candidates
        assertThat(testElection.getCandidates()).hasSize(3);
        assertThat(testElection.getCandidates())
                .extracting(Candidate::getId)
                .containsExactlyInAnyOrder("1-1", "1-2", "1-3");
    }
}
