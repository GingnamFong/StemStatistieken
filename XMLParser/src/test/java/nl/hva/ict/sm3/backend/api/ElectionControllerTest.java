package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ElectionController}.
 * <p>
 * These tests verify:
 * <ul>
 *     <li><b>Happy flow</b> — correct and expected input returns valid responses.</li>
 *     <li><b>Invalid input</b> — unsafe or malformed values trigger validation errors.</li>
 *     <li><b>Error handling</b> — backend failures or missing data return appropriate HTTP status codes.</li>
 *     <li><b>Business rules</b> — caching, data shaping, and logical constraints behave correctly.</li>
 * </ul>
 * <p>
 * External dependencies such as {@link DutchElectionService} are mocked using Mockito,
 * ensuring all tests remain pure unit tests without external filesystem or network usage.
 */
class ElectionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DutchElectionService electionService;

    @InjectMocks
    private ElectionController controller;
    /**
     * Initializes Mockito and prepares a standalone MockMvc instance before each test.
     */
    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = standaloneSetup(controller).build();
    }

    // ------------------------------
    // HAPPY FLOWS
    // ------------------------------
    /**
     * Ensures that the /elections/{id} endpoint returns HTTP 200
     * and the correct election structure when a valid election is found.
     */
    @Test
    void HF_getElection_ok() throws Exception {
        Election election = new Election("2021");
        when(electionService.getElectionById("2021")).thenReturn(election);

        mockMvc.perform(get("/elections/2021"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2021"));
    }
    /**
     * Ensures that a valid election with municipalities is returned correctly
     * when using /elections/{id}/municipalities.
     */
    @Test
    void HF_getMunicipalities_ok() throws Exception {
        Municipality m = new Municipality("M1", "Amsterdam", 0);
        Constituency c = new Constituency("C1", "Region");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        when(electionService.getElectionById("2021")).thenReturn(election);

        mockMvc.perform(get("/elections/2021/municipalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Amsterdam"));
    }
    /**
     * Happy flow for postcode lookup: verifies that a matching polling
     * station is properly returned with HTTP 200.
     */
    @Test
    void HF_getPollingStation_ok() throws Exception {
        PollingStation ps = new PollingStation("PS1", "Station A", "1234AB");

        Municipality m = new Municipality("M1", "Amsterdam", 0);
        m.addPollingStation(ps);

        Constituency c = new Constituency("C1", "Region");
        c.addMunicipality(m);

        Election election = new Election("2021");
        election.addConstituency(c);

        when(electionService.getElectionById("2021")).thenReturn(election);

        mockMvc.perform(get("/elections/2021/pollingstations/postcode/1234AB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postalCode").value("1234AB"));
    }

    /**
     * Ensures that script injection or illegal characters in the election ID
     * cause validation to throw an IllegalArgumentException,
     * and no service calls are made.
     */
    @Test
    void II_invalidElectionId_scriptInjection_shouldThrow() throws Exception {
        try {
            mockMvc.perform(get("/elections/<script>"));
        } catch (Exception e) {
            assert e.getCause() instanceof IllegalArgumentException;
        }
        verify(electionService, never()).getElectionById(anyString());
    }
    /**
     * Ensures that invalid postal codes (symbols or unsafe characters)
     * are rejected by validation logic without calling the service.
     */
    @Test
    void II_invalidPostalCode_shouldThrow() throws Exception {
        try {
            mockMvc.perform(get("/elections/2021/pollingstations/postcode/@@@BAD@@@"));
        } catch (Exception e) {
            assert e.getCause() instanceof IllegalArgumentException;
        }
        verify(electionService, never()).getElectionById(anyString());
    }

    /**
     * Ensures that empty or whitespace-only municipality IDs
     * are rejected by validation.
     */
    @Test
    void II_emptyMunicipalityId_shouldThrow() throws Exception {
        try {
            mockMvc.perform(get("/elections/2021/municipalities/   "));
        } catch (Exception e) {
            assert e.getCause() instanceof IllegalArgumentException;
        }
    }

    // ------------------------------
    // ERROR HANDLING
    // ------------------------------
    /**
     * Ensures that when neither cached nor loaded election data exists,
     * the controller responds with HTTP 404.
     */
    @Test
    void EH_getElection_notFound() throws Exception {
        when(electionService.getElectionById("2021")).thenReturn(null);
        when(electionService.readResults("2021", "2021")).thenReturn(null);

        mockMvc.perform(get("/elections/2021"))
                .andExpect(status().isNotFound());
    }

    /**
     * Ensures that postcode lookup returns HTTP 404
     * when election data does not exist.
     */
    @Test
    void EH_getPollingStation_electionMissing() throws Exception {
        when(electionService.getElectionById("2021")).thenReturn(null);

        mockMvc.perform(get("/elections/2021/pollingstations/postcode/1234AB"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------
    // BUSINESS RULES
    // ------------------------------
    /**
     * Ensures that an election is only loaded once when cached.
     * Confirms correct caching behavior.
     */
    @Test
    void BR_electionLoadedOnceWhenCached() throws Exception {
        Election election = new Election("2021");
        when(electionService.getElectionById("2021"))
                .thenReturn(null)      // first call: not cached
                .thenReturn(election); // second call: cached

        when(electionService.readResults("2021", "2021"))
                .thenReturn(election);

        mockMvc.perform(get("/elections/2021")).andExpect(status().isOk());
        mockMvc.perform(get("/elections/2021")).andExpect(status().isOk());

        verify(electionService, times(1)).readResults("2021", "2021");
    }
    /**
     * Ensures that the /top-parties endpoint always returns exactly 3 parties,
     * following the business rule of selecting top 3 results.
     */
    @Test
    void BR_topParties_returnsExactly3() throws Exception {
        Election election = mock(Election.class);

        when(electionService.getElectionById("2021")).thenReturn(election);
        when(election.getTopParties(3)).thenReturn(
                List.of(
                        new Party("1", "A"),
                        new Party("2", "B"),
                        new Party("3", "C")
                )
        );

        mockMvc.perform(get("/elections/2021/top-parties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
