package nl.hva.ict.sm3.backend.service;

import nl.hva.ict.sm3.backend.model.Constituency;
import nl.hva.ict.sm3.backend.model.Election;
import nl.hva.ict.sm3.backend.model.Municipality;
import nl.hva.ict.sm3.backend.model.PollingStation;

public class MunicipalityService {

    private final Election election;

    public MunicipalityService(Election election) {
        this.election = election;
    }

    public PollingStation findPollingStationByPostalCode(String postalCode) {
        String normalized = postalCode.replace(" ", "").toUpperCase();

        for (Constituency c : election.getConstituencies()) {
            for (Municipality m : c.getMunicipalities()) {
                for (PollingStation ps : m.getPollingStations()) {

                    if (ps.getPostalCode() == null) continue;

                    String normalizedPs = ps.getPostalCode()
                            .replace(" ", "")
                            .toUpperCase();

                    if (normalizedPs.equals(normalized)) {
                        System.out.println("MATCH FOUND: " + normalized);
                        return ps;
                    }
                }
            }
        }

        System.out.println("NO MATCH FOR " + normalized);
        return null;
    }



    public Municipality findMunicipalityByPostalCode(String postalCode) {
        if (postalCode == null) return null;

        for (var constituency : election.getConstituencies()) {
            for (var municipality : constituency.getMunicipalities()) {
                for (var station : municipality.getPollingStations()) {
                    if (postalCode.equalsIgnoreCase(station.getPostalCode())) {
                        return municipality;
                    }
                }
            }
        }
        return null;
    }
}
