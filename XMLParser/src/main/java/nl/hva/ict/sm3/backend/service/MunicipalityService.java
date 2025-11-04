package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MunicipalityService {

    private final DutchElectionService dutchElectionService;

    public MunicipalityService(DutchElectionService dutchElectionService) {
        this.dutchElectionService = dutchElectionService;
    }

    public List<Municipality> getAllMunicipalities(String electionId) {
        Election election = dutchElectionService.getElectionById(electionId);
        if (election == null) return List.of();

        return election.getConstituencies().stream()
                .flatMap(c -> c.getMunicipalities().stream())
                .collect(Collectors.toList());
    }

    public Optional<Municipality> getMunicipalityById(String electionId, String municipalityId) {
        return getAllMunicipalities(electionId).stream()
                .filter(m -> m.getId().equalsIgnoreCase(municipalityId))
                .findFirst();
    }
}
