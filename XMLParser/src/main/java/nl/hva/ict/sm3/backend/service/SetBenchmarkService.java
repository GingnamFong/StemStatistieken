package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.Party;

import java.util.*;

public class SetBenchmarkService {
    /**
     * Runs benchmark using actual election data parsed from XML
     */
    public void runBenchmarkOnRealData(Election election) {
        System.out.println("\n=== HASHSET vs TREESET BENCHMARK (REAL ELECTION DATA) ===");

        // Extract parties from election model
        List<Party> parties = extractAllParties(election);
        System.out.println("Loaded " + parties.size() + " party entries from election XML.");

        benchmarkParties(parties);
    }

    // ---- REAL DATA EXTRACTION -------------------------------------------------------------

    private List<Party> extractAllParties(Election election) {
        List<Party> result = new ArrayList<>();

        for (Constituency constituency : election.getConstituencies()) {
            for (Municipality municipality : constituency.getMunicipalities()) {
                result.addAll(municipality.getAllParties());
            }
        }

        return result;
    }

    // ---- BENCHMARK FOR REAL DATA ----------------------------------------------------------

    private void benchmarkParties(List<Party> parties) {
        Set<Party> hashSet = new HashSet<>();
        Set<Party> treeSet = new TreeSet<>(Comparator.comparing(Party::getName));

        System.out.println("\n[ADD] Measuring insertion performance...");
        double hashAdd = measureAdd(hashSet, parties);
        double treeAdd = measureAdd(treeSet, parties);

        System.out.printf("HashSet add:  %.3f ms\n", hashAdd);
        System.out.printf("TreeSet add:  %.3f ms\n", treeAdd);

        System.out.println("\n[CONTAINS] Measuring search performance...");
        double hashContains = measureContains(hashSet, parties);
        double treeContains = measureContains(treeSet, parties);

        System.out.printf("HashSet contains: %.3f ms\n", hashContains);
        System.out.printf("TreeSet contains: %.3f ms\n", treeContains);

        System.out.println("\n[REMOVE] Measuring removal performance...");
        double hashRemove = measureRemove(hashSet, parties);
        double treeRemove = measureRemove(treeSet, parties);

        System.out.printf("HashSet remove: %.3f ms\n", hashRemove);
        System.out.printf("TreeSet remove: %.3f ms\n", treeRemove);
    }



    // ---- SYNTHETIC BENCHMARK (OPTIONAL) ---------------------------------------------------

    private void benchmarkSynthetic(int amount) {
        Set<Party> hashSet = new HashSet<>();
        Set<Party> treeSet = new TreeSet<>(Comparator.comparing(Party::getName));

        List<Party> parties = generateParties(amount);

        double hashAdd = measureAdd(hashSet, parties);
        double treeAdd = measureAdd(treeSet, parties);

        System.out.println("HashSet add: " + hashAdd + " ms");
        System.out.println("TreeSet add: " + treeAdd + " ms");

        double hashContains = measureContains(hashSet, parties);
        double treeContains = measureContains(treeSet, parties);

        System.out.println("HashSet contains: " + hashContains + " ms");
        System.out.println("TreeSet contains: " + treeContains + " ms");

        double hashRemove = measureRemove(hashSet, parties);
        double treeRemove = measureRemove(treeSet, parties);

        System.out.println("HashSet remove: " + hashRemove + " ms");
        System.out.println("TreeSet remove: " + treeRemove + " ms");
    }

    // ---- PERFORMANCE MEASUREMENT UTILS ----------------------------------------------------

    private double measureAdd(Set<Party> set, List<Party> parties) {
        long start = System.nanoTime();
        for (Party p : parties) set.add(p);
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private double measureContains(Set<Party> set, List<Party> parties) {
        long start = System.nanoTime();
        for (Party p : parties) set.contains(p);
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private double measureRemove(Set<Party> set, List<Party> parties) {
        long start = System.nanoTime();
        for (Party p : parties) set.remove(p);
        return (System.nanoTime() - start) / 1_000_000.0;
    }

    private List<Party> generateParties(int amount) {
        Random random = new Random();
        List<Party> list = new ArrayList<>(amount);

        for (int i = 0; i < amount; i++) {
            String id = "P" + i;
            String name = "Party" + (char) ('A' + (i % 26));  // repeats A–Z
            int votes = random.nextInt(50_000);

            list.add(new Party(id, name, votes));
        }

        return list;
    }
}
