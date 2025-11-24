    package nl.hva.ict.sm3.backend.service;

    import nl.hva.ict.sm3.backend.model.*;
    import org.junit.jupiter.api.BeforeEach;
    import org.junit.jupiter.api.Test;
    import org.junit.jupiter.api.extension.ExtendWith;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;

    import java.util.List;

    import static org.assertj.core.api.Assertions.*;
    import static org.mockito.Mockito.*;

    /**
     * Unit tests voor ProvincieService.
     * - Happy flows: normale input die zou moeten werken
     * - Invalid input: foute input die niet zou moeten werken
     * - Error handling: wat gebeurt er als er iets mis gaat
     * - Business rules: belangrijke regels zoals cache en aggregatie
     */
    @ExtendWith(MockitoExtension.class)
    class ProvincieServiceTest {

        // Dit is een mock versie van DutchElectionService
        // We gebruiken dit zodat we niet de echte service hoeven te gebruiken
        @Mock
        private DutchElectionService electionService;

        // Dit is de echte ProvincieService die we testen
        // Mockito vult automatisch de electionService in
        @InjectMocks
        private ProvincieService provincieService;

        // Test data die we in meerdere tests gebruiken
        private Election testElection;

        @BeforeEach
        void setUp() {
            // Deze methode wordt voor elke test uitgevoerd
            // Hier maken we test data aan die we nodig hebben
            testElection = new Election("TK2023");
            
            // test data: kieskring Groningen met gemeente
            // Dit is nep data om mee te testen
            Constituency groningenKieskring = new Constituency("01", "Groningen");
            Municipality groningenGemeente = new Municipality("0014", "Groningen", 0);
            // Voeg stemmen toe: VVD krijgt 1000 stemmen, D66 krijgt 500
            groningenGemeente.addVotesForParty("1", "VVD", 1000);
            groningenGemeente.addVotesForParty("2", "D66", 500);
            groningenKieskring.addMunicipality(groningenGemeente);
            testElection.addConstituency(groningenKieskring);

            // test data: kieskring Nijmegen die hoort bij Gelderland
            // Gelderland heeft 2 kieskringen: Nijmegen en Arnhem
            Constituency nijmegenKieskring = new Constituency("06", "Nijmegen");
            Municipality nijmegenGemeente = new Municipality("0268", "Nijmegen", 0);
            nijmegenGemeente.addVotesForParty("1", "VVD", 2000);
            nijmegenGemeente.addVotesForParty("3", "PVDA", 1500);
            nijmegenKieskring.addMunicipality(nijmegenGemeente);
            testElection.addConstituency(nijmegenKieskring);

            // Maak test data: kieskring Arnhem (hoort ook bij Gelderland)
            Constituency arnhemKieskring = new Constituency("07", "Arnhem");
            Municipality arnhemGemeente = new Municipality("0202", "Arnhem", 0);
            arnhemGemeente.addVotesForParty("1", "VVD", 1800);
            arnhemGemeente.addVotesForParty("2", "D66", 1200);
            arnhemKieskring.addMunicipality(arnhemGemeente);
            testElection.addConstituency(arnhemKieskring);
        }

        // HAPPY FLOWS
        @Test
        void testGetAllProvinciesForElection_HappyFlow() {
            // Act: roep de methode aan die getest worden
            List<Province> provinces = provincieService.getAllProvinciesForElection("TK2023");

            // Assert: controleer of het resultaat klopt
            assertThat(provinces).isNotNull();
            assertThat(provinces).hasSize(12);
            // Check of de lijst Groningen, Gelderland en Noord-Holland heeft
            assertThat(provinces).extracting(Province::getName)
                    .contains("Groningen", "Gelderland", "Noord-Holland");
            

            Province groningen = provinces.stream()
                    .filter(p -> p.getName().equals("Groningen"))
                    .findFirst()
                    .orElse(null);
            assertThat(groningen).isNotNull();
            assertThat(groningen.getParties()).isEmpty();
        }

        @Test
        void testGetProvincieDataForElection_HappyFlow() {
            // Arrange: zet de mock service klaar
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: roep de methode aan die we testen
            Province province = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: controleer of alles klopt
            assertThat(province).isNotNull(); // Provincie moet bestaan
            assertThat(province.getName()).isEqualTo("Groningen"); // Naam moet kloppen
            assertThat(province.getConstituencyCount()).isEqualTo(1); // 1 kieskring (Groningen)
            assertThat(province.hasConstituency("Groningen")).isTrue(); // Heeft Groningen kieskring
            assertThat(province.getParties()).isNotEmpty(); // Moet partijen hebben
            // Totaal stemmen = 1000 (VVD) + 500 (D66) = 1500
            assertThat(province.getTotalVotes()).isEqualTo(1500);
        }

        // INVALID INPUT
        @Test
        void testGetProvincieDataForElection_OnbekendeProvincie() {
            // Arrange: zet mock klaar
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: vraag een provincie op die niet bestaat
            Province province = provincieService.getProvincieDataForElection("TK2023", "OnbekendeProvincie");

            // Assert: de service moet een provincie object teruggeven maar zonder data
            // De service crasht niet maar geeft gewoon een lege provincie terug
            assertThat(province).isNotNull();
            assertThat(province.getName()).isEqualTo("OnbekendeProvincie");
            assertThat(province.getConstituencyCount()).isEqualTo(0); // 0 kieskringen
            assertThat(province.getParties()).isEmpty(); // 0 partijen
            assertThat(province.getTotalVotes()).isEqualTo(0); // 0 stemmen
        }

        //ERROR HANDLING
        @Test
        void testGetProvincieDataForElection_ElectionNietGevonden() {
            // Arrange: zet mock zo dat beide methoden null teruggeven
            // Zo wordt election niet gevonden
            when(electionService.getElectionById("TK2023")).thenReturn(null);
            when(electionService.readResults("TK2023", "TK2023")).thenReturn(null);

            // Act: vraag provincie data op ook al bestaat de election niet
            Province province = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: de service moet niet crashen maar een lege provincie teruggeven
            assertThat(province).isNotNull();
            assertThat(province.getName()).isEqualTo("Groningen");
            assertThat(province.getParties()).isEmpty(); // Geen data omdat election niet gevonden
            assertThat(province.getTotalVotes()).isEqualTo(0);
            
            // Verify: check of de service beide methoden heeft aangeroepen
            verify(electionService).getElectionById("TK2023");
            verify(electionService).readResults("TK2023", "TK2023");
        }

        // BUSINESS RULES
        @Test
        void testProvincieCache_MeerdereAanroepen() {
            // Arrange: zet mock klaar
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: vraag dezelfde provincie 2 keer op
            Province province1 = provincieService.getProvincieDataForElection("TK2023", "Groningen");
            Province province2 = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: beide moeten hetzelfde object zijn
            // data wordt niet 2 keer opgehaald omdat de cache werkt
            assertThat(province1).isSameAs(province2);
            
            // Verify: check of getElectionById maar 1x is aangeroepen
            // Als cache werkt hoef je de data maar 1x op te halen
            verify(electionService, times(1)).getElectionById("TK2023");
        }

        @Test
        void testAggregatie_ZelfdePartijInMeerdereKieskringen() {
            // Arrange: VVD komt voor in beide kieskringen van Gelderland
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act
            Province province = provincieService.getProvincieDataForElection("TK2023", "Gelderland");

            // Assert: VVD stemmen moeten opgeteld zijn
            // Gelderland heeft 2 kieskringen: Nijmegen (2000 VVD) + Arnhem (1800 VVD) = 3800 totaal
            Party vvd = province.getPartyById("1");
            assertThat(vvd).isNotNull();
            assertThat(vvd.getVotes()).isEqualTo(3800); // 2000 + 1800
        }
    }
