package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Candidate;
import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.Party;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests voor ElectionRepository.
 * These tests check if the repository works correctly.
 * - Happy flows: normal queries that should work
 * - Invalid input: wrong input that should not work
 * - Error handling: what happens when something goes wrong
 * - Business rules: important rules like cascade and pagination
 */
@DataJpaTest
class ElectionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ElectionRepository electionRepository;

    private Election testElection;

    @BeforeEach
    void setUp() {
        // Create a test election with some basic data
        testElection = new Election("TK2023");
        
        // Add a constituency
        Constituency groningenKieskring = new Constituency("01", "Groningen");
        Municipality groningenGemeente = new Municipality("0014", "Groningen", 0);
        groningenGemeente.addVotesForParty("1", "VVD", 1000);
        groningenGemeente.addVotesForParty("2", "D66", 500);
        groningenKieskring.addMunicipality(groningenGemeente);
        testElection.addConstituency(groningenKieskring);
        
        // Add parties
        Party vvd = new Party("1", "VVD");
        Party d66 = new Party("2", "D66");
        testElection.addParty(vvd);
        testElection.addParty(d66);
        
        // Add a candidate
        Candidate candidate = new Candidate("1-1", "Mark", "Rutte", "M.", "Den Haag", "1", "VVD", 1);
        testElection.addCandidate(candidate);
    }

    // HAPPY FLOWS

    @Test
    void testFindById_HappyFlow() {
        // Arrange
        electionRepository.save(testElection);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<Election> found = electionRepository.findById("TK2023");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo("TK2023");
    }

    @Test
    void testFindAllElections_HappyFlow() {
        // Arrange: create multiple elections
        Election election2021 = new Election("TK2021");
        electionRepository.save(testElection);
        electionRepository.save(election2021);
        entityManager.flush();
        entityManager.clear();

        // Act
        List<Election> elections = electionRepository.findAllElections();

        // Assert
        assertThat(elections).hasSize(2);
        assertThat(elections).extracting(Election::getId)
                .containsExactlyInAnyOrder("TK2023", "TK2021");
    }

    // INVALID INPUT

    @Test
    void testFindById_ElectionBestaatNiet() {
        // Act
        Optional<Election> found = electionRepository.findById("NIET_BESTAAND");

        // Assert
        assertThat(found).isEmpty();
    }

    // ERROR HANDLING

    @Test
    void testFindByIdWithDetails_NonExistentElection() {
        // Act: try to find election that does not exist
        Optional<Election> found = electionRepository.findByIdWithDetails("NIET_BESTAAND");

        // Assert: should return empty without throwing exception
        assertThat(found).isEmpty();
    }

    // BUSINESS RULES

    @Test
    void testCascadeDelete() {
        // Arrange
        electionRepository.save(testElection);
        entityManager.flush();
        entityManager.clear();

        // Verify election exists
        assertThat(electionRepository.findById("TK2023")).isPresent();

        // Act: delete election
        electionRepository.deleteById("TK2023");
        entityManager.flush();
        entityManager.clear();

        // Assert: election should be deleted
        assertThat(electionRepository.findById("TK2023")).isEmpty();
    }

    @Test
    void testUpdateElection() {
        // Arrange: save first version
        electionRepository.save(testElection);
        entityManager.flush();
        entityManager.clear();

        // Act: fetch, add party, and save again
        Election election = electionRepository.findById("TK2023").orElseThrow();
        Party pvda = new Party("3", "PVDA");
        election.addParty(pvda);
        electionRepository.save(election);
        entityManager.flush();
        entityManager.clear();

        // Assert: check if the new party was added
        Election updated = electionRepository.findById("TK2023").orElseThrow();
        assertThat(updated.getParties()).hasSize(3);
        assertThat(updated.getParties()).extracting(Party::getName)
                .containsExactlyInAnyOrder("VVD", "D66", "PVDA");
    }
}
