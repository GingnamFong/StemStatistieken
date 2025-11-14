package nl.hva.ict.sm3.backend;

import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.service.DutchElectionService;
import nl.hva.ict.sm3.backend.service.SetBenchmarkService;

public class DemoMain {
    public static void main(String[] args) {
        DutchElectionService electionService = new DutchElectionService();

        // Load real XML (TK2023)
        Election election = electionService.readResults(
                "TK2023",
                "TK2023" // folder in resources
        );

        SetBenchmarkService bench = new SetBenchmarkService();
        bench.runBenchmarkOnRealData(election);
    }
}
