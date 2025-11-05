package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Geaggregeerde resultaten voor een provincie: totaalstemmen en partijen met percentage.
 */
public class ProvinceResults {
    private final int totalVotes;
    private final List<PartyResult> parties;

    public ProvinceResults(int totalVotes, List<Party> parties) {
        this.totalVotes = totalVotes;
        this.parties = new ArrayList<>();
        for (Party party : parties) {
            double percentage = totalVotes > 0 
                ? (party.getVotes() * 100.0 / totalVotes) 
                : 0.0;
            this.parties.add(new PartyResult(party.getName(), party.getVotes(), percentage));
        }
    }

    public int getTotalVotes() {
        // Geeft totaal aantal stemmen in de provincie
        return totalVotes;
    }

    public List<PartyResult> getParties() {
        // Geeft de partijresultaten (naam, stemmen, percentage)
        return new ArrayList<>(parties);
    }

    /**
     * Inner class representing a party result with votes and percentage.
     */
    public static class PartyResult {
        private final String name;
        private final int votes;
        private final double percentage;

        public PartyResult(String name, int votes, double percentage) {
            this.name = name;
            this.votes = votes;
            this.percentage = percentage;
        }

        public String getName() {
            // Partijnaam
            return name;
        }

        public int getVotes() {
            // Aantal stemmen voor deze partij
            return votes;
        }

        public double getPercentage() {
            // Percentage van totaalstemmen
            return percentage;
        }

        public String getPercentageFormatted() {
            // Percentage als tekst (bijv. 12,3)
            return String.format("%.1f", percentage);
        }
    }
}

