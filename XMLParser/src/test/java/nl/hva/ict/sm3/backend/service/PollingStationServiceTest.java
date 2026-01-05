package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PollingStationService}.
 *
 * <p>Tests all core methods of the service:
 * <ul>
 *     <li>{@link PollingStationService#findByPostalCode(String, String)}</li>
 *     <li>{@link PollingStationService#findMunicipalityByPostalCode(String, String)}</li>
 *     <li>{@link PollingStationService#getAllPollingStations(String)}</li>
 * </ul>
 * Both happy and unhappy paths are covered, and {@link DutchElectionService} is mocked.
 * </p>
 */
class PollingStationServiceTest {

    private DutchElectionService mockElectionService;
    private PollingStationService service;

    private Election election;
    private PollingStation ps1;
    private PollingStation ps2;
    private Municipality municipality;
    private Constituency constituency;

    @BeforeEach
    void setUp() {
        // Mock the election service
        mockElectionService = mock(DutchElectionService.class);

        // Create polling stations
        ps1 = new PollingStation("PS1", "Station 1", "1234AB");
        ps2 = new PollingStation("PS2", "Station 2", "5678CD");

        // Create municipality and add polling stations
        municipality = new Municipality("M1", "Amsterdam", 0);
        municipality.addPollingStation(ps1);
        municipality.addPollingStation(ps2);

        // Create constituency and add municipality
        constituency = new Constituency("C1", "Noord-Holland");
        constituency.addMunicipality(municipality);

        // Create election and add constituency
        election = new Election("E1");
        election.addConstituency(constituency);

        // Mock election service to return this election
        when(mockElectionService.getElectionById("E1")).thenReturn(election);
        when(mockElectionService.getElectionById("UNKNOWN")).thenReturn(null);

        // Initialize PollingStationService with the mocked DutchElectionService
        service = new PollingStationService(mockElectionService);
    }

    /**
     * Happy path: finds a polling station by a valid postal code.
     */
    @Test
    void findByPostalCode_returnsPollingStation() {
        // Act
        PollingStation result = service.findByPostalCode("E1", "1234AB");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("PS1");
    }

    /**
     * Unhappy path: returns null for a postal code not found.
     */
    @Test
    void findByPostalCode_returnsNullForUnknownCode() {
        PollingStation result = service.findByPostalCode("E1", "9999ZZ");
        assertThat(result).isNull();
    }

    /**
     * Unhappy path: returns null for an election not found.
     */
    @Test
    void findByPostalCode_returnsNullForUnknownElection() {
        PollingStation result = service.findByPostalCode("UNKNOWN", "1234AB");
        assertThat(result).isNull();
    }

    /**
     * Happy path: finds the municipality containing a polling station.
     */
    @Test
    void findMunicipalityByPostalCode_returnsMunicipality() {
        Municipality result = service.findMunicipalityByPostalCode("E1", "1234AB");
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("M1");
    }

    /**
     * Unhappy path: returns null for a postal code not found in any municipality.
     */
    @Test
    void findMunicipalityByPostalCode_returnsNullForUnknownCode() {
        Municipality result = service.findMunicipalityByPostalCode("E1", "9999ZZ");
        assertThat(result).isNull();
    }

    /**
     * Unhappy path: returns null if election does not exist.
     */
    @Test
    void findMunicipalityByPostalCode_returnsNullForUnknownElection() {
        Municipality result = service.findMunicipalityByPostalCode("UNKNOWN", "1234AB");
        assertThat(result).isNull();
    }

    /**
     * Happy path: returns all polling stations in an election.
     */
    @Test
    void getAllPollingStations_returnsAllStations() {
        List<PollingStation> stations = service.getAllPollingStations("E1");
        assertThat(stations).hasSize(2);
        assertThat(stations).contains(ps1, ps2);
    }

    /**
     * Unhappy path: returns empty list if election not found.
     */
    @Test
    void getAllPollingStations_returnsEmptyListForUnknownElection() {
        List<PollingStation> stations = service.getAllPollingStations("UNKNOWN");
        assertThat(stations).isEmpty();
    }
}
