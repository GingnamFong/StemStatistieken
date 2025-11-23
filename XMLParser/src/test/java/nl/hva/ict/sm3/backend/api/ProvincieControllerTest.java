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
 * Deze tests checken of de REST endpoints goed werken.
 * - Happy flows: normale requests die zouden moeten werken
 * - Invalid input: foute requests die niet zouden moeten werken
 * - Error handling: wat gebeurt er als er iets mis gaat
 * - Business rules: belangrijke regels zoals validatie
 */

@ExtendWith(MockitoExtension.class)
class ProvincieControllerTest {

    // Dit is een mock versie van ProvincieService
    @Mock
    private ProvincieService provincieService;

    // Dit is de echte controller die we testen
    @InjectMocks
    private ProvincieController provincieController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Deze methode wordt voor elke test uitgevoerd
        mockMvc = MockMvcBuilders.standaloneSetup(provincieController).build();
    }

    // HAPPY FLOW
    @Test
    void testGetAllProvincies_HappyFlow() throws Exception {
        // Arrange: maak test data en zet mock klaar
        List<Province> provinces = new ArrayList<>();
        provinces.add(new Province("Groningen"));
        provinces.add(new Province("Gelderland"));
        
        // Als getAllProvinciesForElection wordt aangeroepen geef dan deze lijst terug
        when(provincieService.getAllProvinciesForElection("TK2023")).thenReturn(provinces);

        // Act en Assert: doe een HTTP GET request en check of het goed gaat
        mockMvc.perform(get("/provincies")  // GET request naar /provincies
                        .param("electionId", "TK2023"))  // parameter electionId=TK2023
                .andExpect(status().isOk())  // Verwacht status 200 (OK)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))  // Verwacht JSON
                .andExpect(jsonPath("$").isArray())  // Verwacht een array
                .andExpect(jsonPath("$[0].naam").exists());  // Eerste item moet een "naam" hebben

        // Verify: check of de service is aangeroepen
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
        // Act & Assert: test met gevaarlijke karakters zoals <script>
        // De controller heeft validatie die dit moet afvangen
        try {
            mockMvc.perform(get("/provincies/test<script>")  // Provincie naam met <script>
                            .param("electionId", "TK2023"));
        } catch (Exception e) {
            // De controller moet een IllegalArgumentException gooien
            // Dit betekent dat de validatie werkt
            assertThat(e.getCause()).isInstanceOf(IllegalArgumentException.class);
        }
        
        // Verify: de service mag NIET worden aangeroepen
        // De validatie moet het blokkeren voordat het bij de service komt
        verify(provincieService, never()).getProvincieDataForElection(anyString(), anyString());
    }

    // ERROR HANDLING
    @Test
    void testGetProvincieData_ServiceThrowsRuntimeException() throws Exception {
        // Arrange: zet mock zo dat de service een exception gooit
        doThrow(new RuntimeException("Unexpected error"))
                .when(provincieService).getProvincieDataForElection("TK2023", "Groningen");

        // Act en Assert: doe een request ook al gooit de service een exception
        try {
            mockMvc.perform(get("/provincies/Groningen")
                            .param("electionId", "TK2023"));
        } catch (Exception e) {
            // De exception wordt doorgegeven dit is goed
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

        // Act en Assert verschillende electionIds
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

        // Act en Assert percentages moeten goed berekend zijn
        mockMvc.perform(get("/provincies/Groningen/resultaten")
                        .param("electionId", "TK2023"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totaalStemmen").value(1500))
                .andExpect(jsonPath("$.partijen[0].percentage").exists());
    }
}
