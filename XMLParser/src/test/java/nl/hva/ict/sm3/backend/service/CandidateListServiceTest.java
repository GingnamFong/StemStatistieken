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
 * - Happy flows: normale input
 * - Cache logic: election al in cache
 * - Error handling: als er iets mis gaat
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

    // HAPPY FLOWS
    @Test
    void testLoadCandidateLists_HappyFlow_NotCached() {
        // Arrange: Geen cached election
        when(electionService.getElectionById("TK2023")).thenReturn(null);

        // Act
        candidateListService.loadCandidateLists(testElection, "TK2023");

        // Assert: verify dat getElectionById is aangeroepen
        verify(electionService, times(1)).getElectionById("TK2023");

    }

    @Test
    void testLoadCandidateLists_HappyFlow_CachedWithCandidates() {
        // Arrange: Cached candidates
        cachedElection.getCandidates().addAll(testCandidates);
        when(electionService.getElectionById("TK2023")).thenReturn(cachedElection);

        // Act
        candidateListService.loadCandidateLists(testElection, "TK2023");
        
        // Assert: verify candidates zijn geadd
        verify(electionService, times(1)).getElectionById("TK2023");

        assertThat(testElection.getCandidates()).hasSize(3);
        assertThat(testElection.getCandidates())
                .extracting(Candidate::getId)
                .containsExactly("1-1", "1-2", "1-3");
        
        // verify geen nieuwe cache
        verify(electionService, never()).cacheElection(anyString(), any(Election.class));
    }

