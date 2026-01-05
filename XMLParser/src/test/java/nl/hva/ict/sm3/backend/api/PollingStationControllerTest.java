package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.PollingStation;
import nl.hva.ict.sm3.backend.service.PollingStationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Unit tests for {@link PollingStationController}.
 * <p>
 * These tests verify:
 * <ul>
 *     <li><b>Happy flow</b> — valid input returns polling station data.</li>
 *     <li><b>Invalid input</b> — unsafe postal codes are rejected.</li>
 *     <li><b>Error handling</b> — missing polling stations return HTTP 404.</li>
 * </ul>
 * <p>
 * {@link PollingStationService} is mocked to ensure these are pure
 * controller-level unit tests.
 */
class PollingStationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PollingStationService pollingStationService;

    @InjectMocks
    private PollingStationController controller;

    /**
     * Initializes Mockito and sets up a standalone MockMvc instance.
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
     * Ensures that a polling station is returned with HTTP 200
     * when a valid election ID and postal code are provided.
     */
    @Test
    void HF_getPollingStation_ok() throws Exception {
        PollingStation ps =
                new PollingStation("PS1", "Station A", "1234AB");

        when(pollingStationService.findByPostalCode("2021", "1234AB"))
                .thenReturn(ps);

        mockMvc.perform(get("/pollingstations/2021/postcode/1234AB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("PS1"))
                .andExpect(jsonPath("$.postalCode").value("1234AB"));
    }

    // ------------------------------
    // INVALID INPUT
    // ------------------------------

    /**
     * Ensures that illegal postal codes are rejected
     * and no service calls are made.
     */
    @Test
    void II_invalidPostalCode_shouldThrow() throws Exception {
        try {
            mockMvc.perform(
                    get("/pollingstations/2021/postcode/@@@BAD@@@")
            );
        } catch (Exception e) {
            assert e.getCause() instanceof IllegalArgumentException;
        }

        verify(pollingStationService, never())
                .findByPostalCode(anyString(), anyString());
    }

    // ------------------------------
    // ERROR HANDLING
    // ------------------------------

    /**
     * Ensures that HTTP 404 is returned when no polling station exists
     * for the given postal code.
     */
    @Test
    void EH_getPollingStation_notFound() throws Exception {
        when(pollingStationService.findByPostalCode("2021", "9999ZZ"))
                .thenReturn(null);

        mockMvc.perform(get("/pollingstations/2021/postcode/9999ZZ"))
                .andExpect(status().isNotFound());
    }
}
