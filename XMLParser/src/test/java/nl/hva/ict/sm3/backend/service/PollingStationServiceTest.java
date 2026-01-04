package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PollingStationServiceTest {

    @Test
    void findByPostalCode_returnsPollingStation() {
        // Arrange
        PollingStation ps = new PollingStation("PS1", "Station 1", "1234AB");
        Municipality municipality = new Municipality("M1", "Amsterdam", 0);
        municipality.addPollingStation(ps);

        Constituency constituency = new Constituency("C1", "Noord-Holland");
        constituency.addMunicipality(municipality);

        Election election = new Election("E1");
        election.addConstituency(constituency);

        PollingStationService service = new PollingStationService(election);

        // Act
        PollingStation result = service.findByPostalCode("1234AB");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("PS1");
    }

    @Test
    void findMunicipalityByPostalCode_returnsMunicipality() {
        // Arrange
        PollingStation ps = new PollingStation("PS1", "Station 1", "1234AB");
        Municipality municipality = new Municipality("M1", "Amsterdam", 0);
        municipality.addPollingStation(ps);

        Constituency constituency = new Constituency("C1", "Noord-Holland");
        constituency.addMunicipality(municipality);

        Election election = new Election("E1");
        election.addConstituency(constituency);

        PollingStationService service = new PollingStationService(election);

        // Act
        Municipality result = service.findMunicipalityByPostalCode("1234AB");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("M1");
    }

    @Test
    void getAllPollingStations_returnsAllStations() {
        // Arrange
        PollingStation ps1 = new PollingStation("PS1", "Station 1", "1234AB");
        PollingStation ps2 = new PollingStation("PS2", "Station 2", "5678CD");
        Municipality municipality = new Municipality("M1", "Amsterdam", 0);
        municipality.addPollingStation(ps1);
        municipality.addPollingStation(ps2);

        Constituency constituency = new Constituency("C1", "Noord-Holland");
        constituency.addMunicipality(municipality);

        Election election = new Election("E1");
        election.addConstituency(constituency);

        PollingStationService service = new PollingStationService(election);

        // Act
        var stations = service.getAllPollingStations();

        // Assert
        assertThat(stations).hasSize(2);
    }
}
