package nl.hva.ict.sm3.backend.model;

/**
 * Enum representing the type of national result data.
 */
public enum NationalResult {
    PARTY_VOTES,    // Regular party vote data
    REJECTED_DATA,  // Rejected votes and total counted data
    SEATS          // Seat allocation data
}