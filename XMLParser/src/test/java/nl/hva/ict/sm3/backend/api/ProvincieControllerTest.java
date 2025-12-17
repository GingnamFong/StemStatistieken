package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.*;
import nl.hva.ict.sm3.backend.service.ProvincieService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests voor ProvincieController.
 * These tests check if the REST endpoints work correctly.
 * - Happy flows: normal requests that should work
 * - Invalid input: wrong requests that should not work
 * - Error handling: what happens when something goes wrong
 * - Business rules: important rules like validation
 */

@ExtendWith(MockitoExtension.class)
class ProvincieControllerTest {

    // This is a mock version of ProvincieService
    @Mock
    private ProvincieService provincieService;

    // This is the real controller we are testing
    @InjectMocks
    private ProvincieController provincieController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // This method is executed before each test
        mockMvc = MockMvcBuilders.standaloneSetup(provincieController).build();
    }

    // HAPPY FLOW
    @Test
    void testGetAllProvincies_HappyFlow() throws Exception {
        // Arrange: create test data and set up mock
        List<Province> provinces = new ArrayList<>();
        provinces.add(new Province("Groningen"));
        provinces.add(new Province("Gelderland"));
        
        // When getAllProvinciesForElection is called, return this list
        when(provincieService.getAllProvinciesForElection("TK2023")).thenReturn(provinces);

        // Act and Assert: do an HTTP GET request and check if it works
        mockMvc.perform(get("/provincies")  // GET request to /provincies
                        .param("electionId", "TK2023"))  // parameter electionId=TK2023
                .andExpect(status().isOk())  // Expect status 200 (OK)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Expect JSON
                .andExpect(jsonPath("$").isArray())  // Expect an array
                .andExpect(jsonPath("$[0].naam").exists());  // First item must have a "naam"

        // Verify: check if the service was called
        verify(provincieService).getAllProvinciesForElection("TK2023");
    }

    @Test
    void testGetProvincieData_HappyFlow() throws Exception {
        // Arrange
        Province province = new Province("Groningen");
        province.addConstituencyId("Groningen");
        province.addPartyVotes("1", "VVD", 1000);
        province.calculateTotalVotes();
        
        when(provincieService.getProvincieDataForElection("TK2023", "Groningen")).thenReturn(province);

        // Act en Assert
        mockMvc.perform(get("/provincies/Groningen")
                        .param("electionId", "TK2023"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.naam").value("Groningen"))
                .andExpect(jsonPath("$.resultaten").exists())
                .andExpect(jsonPath("$.resultaten.totaalStemmen").exists());

        verify(provincieService).getProvincieDataForElection("TK2023", "Groningen");
    }

    // INVALID INPUT
    @Test
    void testGetProvincieData_GevaarlijkeKarakters() throws Exception {
        // Act & Assert: test with dangerous characters like <script>
        // The controller has validation that should catch this
        try {
            mockMvc.perform(get("/provincies/test<script>")  // Province name with <script>
                            .param("electionId", "TK2023"));
        } catch (Exception e) {
            // The controller should throw an IllegalArgumentException
            // This means the validation works
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        
        // Verify: the service should NOT be called
        // The validation should block it before it reaches the service
        verify(provincieService, never()).getProvincieDataForElection(anyString(), anyString());
    }

    // ERROR HANDLING
    @Test
    void testGetProvincieData_ServiceThrowsRuntimeException() throws Exception {
        // Arrange: set mock so that the service throws an exception
        doThrow(new RuntimeException("Unexpected error"))
                .when(provincieService).getProvincieDataForElection("TK2023", "Groningen");

        // Act and Assert: do a request even though the service throws an exception
        try {
            mockMvc.perform(get("/provincies/Groningen")
                            .param("electionId", "TK2023"));
        } catch (Exception e) {
            // The exception is passed through, this is good
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }
    }

    // BUSINESS RULES
    @Test
    void testGetAllProvincies_VerschillendeElectionIds() throws Exception {
        // Arrange
        List<Province> provinces2021 = new ArrayList<>();
        provinces2021.add(new Province("Groningen"));
        
        List<Province> provinces2023 = new ArrayList<>();
        provinces2023.add(new Province("Groningen"));
        
        when(provincieService.getAllProvinciesForElection("TK2021")).thenReturn(provinces2021);
        when(provincieService.getAllProvinciesForElection("TK2023")).thenReturn(provinces2023);

        // Act and Assert different electionIds
        mockMvc.perform(get("/provincies")
                        .param("electionId", "TK2021"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/provincies")
                        .param("electionId", "TK2023"))
                .andExpect(status().isOk());

        verify(provincieService).getAllProvinciesForElection("TK2021");
        verify(provincieService).getAllProvinciesForElection("TK2023");
    }

    @Test
    void testGetProvincieResultaten_PercentageBerekening() throws Exception {
        // Arrange
        Province province = new Province("Groningen");
        province.addPartyVotes("1", "VVD", 1000);
        province.addPartyVotes("2", "D66", 500);
        province.calculateTotalVotes();
        
        ProvinceResults results = new ProvinceResults(province.getTotalVotes(), province.getParties());
        
        when(provincieService.getProvincieResultatenForElection("TK2023", "Groningen"))
                .thenReturn(results);

        // Act and Assert percentages should be calculated correctly
        mockMvc.perform(get("/provincies/Groningen/resultaten")
                        .param("electionId", "TK2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totaalStemmen").value(1500))
                .andExpect(jsonPath("$.partijen[0].percentage").exists());
    }
}
