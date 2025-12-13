package nl.hva.ict.sm3.backend.config;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.repository.ElectionRepository;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * DataInitializer that automatically loads election data on application startup.
 * Only runs in non-test profiles.
 */
@Component
@Profile("!test")
@Order(1)
public class DataInitializer implements CommandLineRunner {
    
    private final DutchElectionService electionService;
    private final ElectionRepository electionRepository;
    
    @Autowired
    public DataInitializer(DutchElectionService electionService, 
                          ElectionRepository electionRepository) {
        this.electionService = electionService;
        this.electionRepository = electionRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n========================================");
        System.out.println("     DataInitializer Starting");
        System.out.println("========================================\n");
        
        // Check current database state
        long currentCount = electionRepository.count();
        System.out.println("Elections currently in database: " + currentCount);
        
        // Elections to load
        String[] electionIds = {"TK2021", "TK2023", "TK2025"};
        
        for (String electionId : electionIds) {
            loadElection(electionId);
        }
        
        // Final status
        long finalCount = electionRepository.count();
        System.out.println("\n========================================");
        System.out.println("     DataInitializer Complete");
        System.out.println("     Elections in database: " + finalCount);
        System.out.println("========================================\n");
    }
    
    private void loadElection(String electionId) {
        try {
            // Let the service handle checking if election exists and is complete
            // Don't check here to avoid lazy loading issues
            
            System.out.println("\nLoading " + electionId + "...");
            
            // Load from XML and save to database
            Election election = electionService.readResults(electionId, electionId);
            
            if (election != null) {
                System.out.println("✓ " + electionId + " loaded successfully");
                
                // Also load candidate lists if needed
                if (election.getCandidates().isEmpty()) {
                    System.out.println("  Loading candidates for " + electionId + "...");
                    electionService.loadCandidateLists(election, electionId);
                }
            } else {
                System.err.println("✗ Failed to load " + electionId);
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error loading " + electionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
