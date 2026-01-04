package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PollingStationRepository}.
 *
 * <p>This class verifies that repository methods behave correctly using mocking.
 * Happy and unhappy paths are covered. No real database is used.</p>
 */
class PollingStationRepositoryTest {

    @Mock
    private PollingStationRepository pollingStationRepository;

    @InjectMocks
    private PollingStationRepositoryTest testInstance; // for example purposes, not used

    private PollingStation station1;
    private PollingStation station2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample polling stations
        station1 = new PollingStation("PS1", "Station 1", "1234AB");
        station2 = new PollingStation("PS2", "Station 2", "5678CD");
    }

    /**
     * Happy path: finds polling stations by municipality ID.
     */
    @Test
    void findByMunicipalityId_returnsStations() {
        when(pollingStationRepository.findByMunicipalityId("M1"))
                .thenReturn(List.of(station1));

        List<PollingStation> result = pollingStationRepository.findByMunicipalityId("M1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("PS1");

        verify(pollingStationRepository, times(1)).findByMunicipalityId("M1");
    }

    /**
     * Unhappy path: no stations for unknown municipality.
     */
    @Test
    void findByMunicipalityId_returnsEmptyListWhenNotFound() {
        when(pollingStationRepository.findByMunicipalityId("UNKNOWN"))
                .thenReturn(List.of());

        List<PollingStation> result = pollingStationRepository.findByMunicipalityId("UNKNOWN");

        assertThat(result).isEmpty();
        verify(pollingStationRepository, times(1)).findByMunicipalityId("UNKNOWN");
    }

    /**
     * Happy path: finds polling stations within a postal code range.
     */
    @Test
    void findByPostalCodeRange_returnsStationsWithinRange() {
        when(pollingStationRepository.findByPostalCodeRange("1000AA", "6000ZZ"))
                .thenReturn(List.of(station1, station2));

        List<PollingStation> result = pollingStationRepository.findByPostalCodeRange("1000AA", "6000ZZ");

        assertThat(result).hasSize(2);
        verify(pollingStationRepository, times(1)).findByPostalCodeRange("1000AA", "6000ZZ");
    }

    /**
     * Unhappy path: no stations in postal code range.
     */
    @Test
    void findByPostalCodeRange_returnsEmptyListWhenNoMatch() {
        when(pollingStationRepository.findByPostalCodeRange("9000ZZ", "9999ZZ"))
                .thenReturn(List.of());

        List<PollingStation> result = pollingStationRepository.findByPostalCodeRange("9000ZZ", "9999ZZ");

        assertThat(result).isEmpty();
        verify(pollingStationRepository, times(1)).findByPostalCodeRange("9000ZZ", "9999ZZ");
    }

    /**
     * Happy path: finds polling stations by election ID and postal code range.
     */
    @Test
    void findByElectionIdAndPostalCodeRange_returnsStations() {
        when(pollingStationRepository.findByElectionIdAndPostalCodeRange("E1", "1000AA", "2000ZZ"))
                .thenReturn(List.of(station1));

        List<PollingStation> result = pollingStationRepository.findByElectionIdAndPostalCodeRange("E1", "1000AA", "2000ZZ");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("PS1");

        verify(pollingStationRepository, times(1))
                .findByElectionIdAndPostalCodeRange("E1", "1000AA", "2000ZZ");
    }

    /**
     * Unhappy path: election exists but no stations in range.
     */
    @Test
    void findByElectionIdAndPostalCodeRange_returnsEmptyListWhenOutOfRange() {
        when(pollingStationRepository.findByElectionIdAndPostalCodeRange("E1", "9000ZZ", "9999ZZ"))
                .thenReturn(List.of());

        List<PollingStation> result = pollingStationRepository.findByElectionIdAndPostalCodeRange("E1", "9000ZZ", "9999ZZ");

        assertThat(result).isEmpty();
        verify(pollingStationRepository, times(1))
                .findByElectionIdAndPostalCodeRange("E1", "9000ZZ", "9999ZZ");
    }
}
