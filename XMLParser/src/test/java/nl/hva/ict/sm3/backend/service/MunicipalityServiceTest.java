package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MunicipalityService}.
 *
 * These tests cover the 4 required categories:
 * <ul>
 *     <li><b>Happy Flow</b> – expected correct behavior</li>
 *     <li><b>Invalid Input</b> – format variations that must still be handled</li>
 *     <li><b>Error Handling</b> – situations where no result should be found</li>
 *     <li><b>Business Rules</b> – domain logic: postal codes map to municipalities</li>
 * </ul>
 */
public class MunicipalityServiceTest {

    /**
     * HAPPY FLOW:
     * Tests that the service returns the correct polling station
     * when the postal code matches exactly.
     */
    @Test
    void findPollingStationByPostalCode_shouldReturnStation_whenMatch() {
        PollingStation ps = new PollingStation("1", "Station A", "1234AB");

        Municipality m = new Municipality("M1", "Amsterdam", 0);
        m.addPollingStation(ps);

        Constituency c = new Constituency("C1", "Region 1");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        MunicipalityService service = new MunicipalityService(election);

        PollingStation result = service.findPollingStationByPostalCode("1234AB");

        assertThat(result).isNotNull();
        assertThat(result.getPostalCode()).isEqualTo("1234AB");
    }

    /**
     * INVALID INPUT:
     * The postal code contains spaces and lowercase characters,
     * but the service should normalize the input and still match.
     */
    @Test
    void findPollingStationByPostalCode_shouldNormalizePostcode() {
        PollingStation ps = new PollingStation("1", "Station B", "12 34 ab");

        Municipality m = new Municipality("M1", "Rotterdam", 0);
        m.addPollingStation(ps);

        Constituency c = new Constituency("C1", "Region 1");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        MunicipalityService service = new MunicipalityService(election);

        PollingStation result = service.findPollingStationByPostalCode("1234AB");

        assertThat(result).isNotNull();
    }

    /**
     * ERROR HANDLING:
     * Postal code does not exist in the election data.
     * The service should return {@code null}, not throw an exception.
     */
    @Test
    void findPollingStationByPostalCode_shouldReturnNull_whenNoMatch() {
        PollingStation ps = new PollingStation("1", "Station C", "5678CD");

        Municipality m = new Municipality("M1", "Utrecht", 0);
        m.addPollingStation(ps);

        Constituency c = new Constituency("C1", "Region 1");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        MunicipalityService service = new MunicipalityService(election);

        PollingStation result = service.findPollingStationByPostalCode("9999ZZ");

        assertThat(result).isNull();
    }

    /**
     * BUSINESS RULE:
     * A postal code belongs to exactly one municipality.
     * This test ensures the service returns the correct municipality
     * based on the polling station's postal code.
     */
    @Test
    void findMunicipalityByPostalCode_shouldReturnMunicipality() {
        PollingStation ps = new PollingStation("1", "Station A", "4444AA");

        Municipality m = new Municipality("M2", "Eindhoven", 0);
        m.addPollingStation(ps);

        Constituency c = new Constituency("C1", "Region 1");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        MunicipalityService service = new MunicipalityService(election);

        Municipality result = service.findMunicipalityByPostalCode("4444AA");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Eindhoven");
    }
}
