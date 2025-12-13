package nl.hva.ict.sm3.backend.mapper;

import nl.hva.ict.sm3.backend.dto.*;
import nl.hva.ict.sm3.backend.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class to convert between JPA entities and DTOs for API responses.
 * Includes null safety checks to prevent errors with invalid data.
 */
@Component
public class ElectionMapper {
    
    /**
     * Converts an Election entity to an ElectionDto.
     * Filters out any National objects with null type.
     */
    public ElectionDto toElectionDto(Election election) {
        if (election == null) {
            return null;
        }
        
        ElectionDto dto = new ElectionDto();
        dto.setId(election.getId());
        
        // Map constituencies
        if (election.getConstituencies() != null) {
            dto.setConstituencies(election.getConstituencies().stream()
                .map(this::toConstituencyDto)
                .collect(Collectors.toList()));
        }
        
        // Map parties
        if (election.getParties() != null) {
            dto.setParties(election.getParties().stream()
                .map(this::toPartyDto)
                .collect(Collectors.toList()));
        }
        
        // Map candidates
        if (election.getCandidates() != null) {
            dto.setCandidates(election.getCandidates().stream()
                .map(this::toCandidateDto)
                .collect(Collectors.toList()));
        }
        
        // Map national votes - FILTER OUT NULL TYPES
        if (election.getNationalVotes() != null) {
            List<NationalDto> nationalDtos = election.getNationalVotes().stream()
                .filter(n -> n != null && n.getType() != null) // Filter out null types
                .map(this::toNationalDto)
                .collect(Collectors.toList());
            
            // Log if we filtered any out
            int filtered = election.getNationalVotes().size() - nationalDtos.size();
            if (filtered > 0) {
                System.err.println("WARNING: Filtered out " + filtered + 
                    " National objects with null type during DTO mapping");
            }
            
            dto.setNationalVotes(nationalDtos);
        }
        
        // Map seat allocations
        dto.setSeatAllocations(election.getSeatAllocations());
        
        return dto;
    }
    
    /**
     * Converts a Constituency entity to a ConstituencyDto.
     */
    public ConstituencyDto toConstituencyDto(Constituency constituency) {
        if (constituency == null) {
            return null;
        }
        
        ConstituencyDto dto = new ConstituencyDto();
        dto.setId(constituency.getId());
        dto.setName(constituency.getName());
        dto.setTotalVotes(constituency.getTotalVotes());
        
        if (constituency.getMunicipalities() != null) {
            dto.setMunicipalities(constituency.getMunicipalities().stream()
                .map(this::toMunicipalityDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Converts a Municipality entity to a MunicipalityDto.
     */
    public MunicipalityDto toMunicipalityDto(Municipality municipality) {
        if (municipality == null) {
            return null;
        }
        
        MunicipalityDto dto = new MunicipalityDto();
        dto.setId(municipality.getId());
        dto.setName(municipality.getName());
        dto.setValidVotes(municipality.getValidVotes());
        
        if (municipality.getPollingStations() != null) {
            dto.setPollingStations(municipality.getPollingStations().stream()
                .map(this::toPollingStationDto)
                .collect(Collectors.toList()));
        }
        
        // Map party results
        if (municipality.getAllParties() != null) {
            dto.setPartyResults(municipality.getAllParties().stream()
                .map(this::toPartyDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Converts a PollingStation entity to a PollingStationDto.
     */
    public PollingStationDto toPollingStationDto(PollingStation station) {
        if (station == null) {
            return null;
        }
        
        PollingStationDto dto = new PollingStationDto();
        dto.setId(station.getId());
        dto.setName(station.getName());
        dto.setPostalCode(station.getPostalCode());
        dto.setValidVotes(station.getValidVotes());
        
        // Map party results
        if (station.getAllParties() != null) {
            dto.setPartyResults(station.getAllParties().stream()
                .map(this::toPartyDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * Converts a Party entity to a PartyDto.
     */
    public PartyDto toPartyDto(Party party) {
        if (party == null) {
            return null;
        }
        
        PartyDto dto = new PartyDto();
        dto.setId(party.getId());
        dto.setName(party.getName());
        dto.setVotes(party.getVotes());
        
        return dto;
    }
    
    /**
     * Converts a Candidate entity to a CandidateDto.
     */
    public CandidateDto toCandidateDto(Candidate candidate) {
        if (candidate == null) {
            return null;
        }
        
        CandidateDto dto = new CandidateDto();
        dto.setId(candidate.getId());
        dto.setFirstName(candidate.getFirstName());
        dto.setLastName(candidate.getLastName());
        dto.setInitials(candidate.getInitials());
        dto.setResidence(candidate.getResidence());
        dto.setPartyId(candidate.getPartyId());
        dto.setPartyName(candidate.getPartyName());
        dto.setCandidateIdentifier(candidate.getCandidateIdentifier());
        dto.setShortCode(candidate.getShortCode());
        dto.setVotes(candidate.getVotes());
        
        return dto;
    }
    
    /**
     * Converts a National entity to a NationalDto.
     * Ensures type is not null before conversion.
     */
    public NationalDto toNationalDto(National national) {
        if (national == null || national.getType() == null) {
            return null; // Skip National objects with null type
        }
        
        // Use the existing NationalDto.from() method which already handles the conversion
        return NationalDto.from(national);
    }
}