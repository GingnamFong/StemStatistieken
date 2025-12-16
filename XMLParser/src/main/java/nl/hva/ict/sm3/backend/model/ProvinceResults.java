package nl.hva.ict.sm3.backend.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated results for a province: total votes and parties with percentage.
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
        // Returns total number of votes in the province
        return totalVotes;
    }

    public List<PartyResult> getParties() {
        // Returns party results (name, votes, percentage)
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
            // Party name
            return name;
        }

        public int getVotes() {
            // Number of votes for this party
            return votes;
        }

        public double getPercentage() {
            // Percentage of total votes
            return percentage;
        }

        public String getPercentageFormatted() {
            // Percentage as text (e.g., 12.3)
            return String.format("%.1f", percentage);
        }
    }
}

