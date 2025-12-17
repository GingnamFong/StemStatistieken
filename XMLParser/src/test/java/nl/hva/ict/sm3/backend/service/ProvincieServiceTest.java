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
     * - Happy flows: normal input that should work
     * - Invalid input: wrong input that should not work
     * - Error handling: what happens when something goes wrong
     * - Business rules: important rules like cache and aggregation
     */
    @ExtendWith(MockitoExtension.class)
    class ProvincieServiceTest {

        // This is a mock version of DutchElectionService
        // We use this so we don't have to use the real service
        @Mock
        private DutchElectionService electionService;

        // This is the real ProvincieService we are testing
        // Mockito automatically fills in the electionService
        @InjectMocks
        private ProvincieService provincieService;

        // Test data that we use in multiple tests
        private Election testElection;

        @BeforeEach
        void setUp() {
            // This method is executed before each test
            // Here we create test data that we need
            testElection = new Election("TK2023");
            
            // test data: constituency Groningen with municipality
            // This is fake data for testing
            Constituency groningenKieskring = new Constituency("01", "Groningen");
            Municipality groningenGemeente = new Municipality("0014", "Groningen", 0);
            // Add votes: VVD gets 1000 votes, D66 gets 500
            groningenGemeente.addVotesForParty("1", "VVD", 1000);
            groningenGemeente.addVotesForParty("2", "D66", 500);
            groningenKieskring.addMunicipality(groningenGemeente);
            testElection.addConstituency(groningenKieskring);

            // test data: constituency Nijmegen which belongs to Gelderland
            // Gelderland has 2 constituencies: Nijmegen and Arnhem
            Constituency nijmegenKieskring = new Constituency("06", "Nijmegen");
            Municipality nijmegenGemeente = new Municipality("0268", "Nijmegen", 0);
            nijmegenGemeente.addVotesForParty("1", "VVD", 2000);
            nijmegenGemeente.addVotesForParty("3", "PVDA", 1500);
            nijmegenKieskring.addMunicipality(nijmegenGemeente);
            testElection.addConstituency(nijmegenKieskring);

            // Create test data: constituency Arnhem (also belongs to Gelderland)
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
            // Act: call the method being tested
            List<Province> provinces = provincieService.getAllProvinciesForElection("TK2023");

            // Assert: check if the result is correct
            assertThat(provinces).isNotNull();
            assertThat(provinces).hasSize(12);
            // Check if the list has Groningen, Gelderland and Noord-Holland
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
            // Arrange: set up the mock service
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: call the method we are testing
            Province province = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: check if everything is correct
            assertThat(province).isNotNull(); // Province must exist
            assertThat(province.getName()).isEqualTo("Groningen"); // Name must be correct
            assertThat(province.getConstituencyCount()).isEqualTo(1); // 1 constituency (Groningen)
            assertThat(province.hasConstituency("Groningen")).isTrue(); // Has Groningen constituency
            assertThat(province.getParties()).isNotEmpty(); // Must have parties
            // Total votes = 1000 (VVD) + 500 (D66) = 1500
            assertThat(province.getTotalVotes()).isEqualTo(1500);
        }

        // INVALID INPUT
        @Test
        void testGetProvincieDataForElection_OnbekendeProvincie() {
            // Arrange: set up mock
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: request a province that does not exist
            Province province = provincieService.getProvincieDataForElection("TK2023", "OnbekendeProvincie");

            // Assert: the service should return a province object but without data
            // The service does not crash but just returns an empty province
            assertThat(province).isNotNull();
            assertThat(province.getName()).isEqualTo("OnbekendeProvincie");
            assertThat(province.getConstituencyCount()).isEqualTo(0); // 0 constituencies
            assertThat(province.getParties()).isEmpty(); // 0 parties
            assertThat(province.getTotalVotes()).isEqualTo(0); // 0 votes
        }

        //ERROR HANDLING
        @Test
        void testGetProvincieDataForElection_ElectionNietGevonden() {
            // Arrange: set mock so that both methods return null
            // This way the election is not found
            when(electionService.getElectionById("TK2023")).thenReturn(null);
            when(electionService.readResults("TK2023", "TK2023")).thenReturn(null);

            // Act: request province data even though the election does not exist
            Province province = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: the service should not crash but return an empty province
            assertThat(province).isNotNull();
            assertThat(province.getName()).isEqualTo("Groningen");
            assertThat(province.getParties()).isEmpty(); // No data because election not found
            assertThat(province.getTotalVotes()).isEqualTo(0);
            
            // Verify: check if the service called both methods
            verify(electionService).getElectionById("TK2023");
            verify(electionService).readResults("TK2023", "TK2023");
        }

        // BUSINESS RULES
        @Test
        void testProvincieCache_MeerdereAanroepen() {
            // Arrange: set up mock
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act: request the same province 2 times
            Province province1 = provincieService.getProvincieDataForElection("TK2023", "Groningen");
            Province province2 = provincieService.getProvincieDataForElection("TK2023", "Groningen");

            // Assert: both should be the same object
            // data is not fetched 2 times because the cache works
            assertThat(province1).isSameAs(province2);
            
            // Verify: check if getElectionById was called only once
            // If cache works you only need to fetch the data once
            verify(electionService, times(1)).getElectionById("TK2023");
        }

        @Test
        void testAggregatie_ZelfdePartijInMeerdereKieskringen() {
            // Arrange: VVD appears in both constituencies of Gelderland
            when(electionService.getElectionById("TK2023")).thenReturn(testElection);

            // Act
            Province province = provincieService.getProvincieDataForElection("TK2023", "Gelderland");

            // Assert: VVD votes should be added together
            // Gelderland has 2 constituencies: Nijmegen (2000 VVD) + Arnhem (1800 VVD) = 3800 total
            Party vvd = province.getPartyById("1");
            assertThat(vvd).isNotNull();
            assertThat(vvd.getVotes()).isEqualTo(3800); // 2000 + 1800
        }
    }
