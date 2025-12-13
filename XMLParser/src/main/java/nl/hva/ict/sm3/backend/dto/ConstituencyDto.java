package nl.hva.ict.sm3.backend.dto;

import java.util.List;

public class ConstituencyDto {
    private String id;
    private String name;
    private int totalVotes;
    private List<MunicipalityDto> municipalities;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public List<MunicipalityDto> getMunicipalities() {
        return municipalities;
    }

    public void setMunicipalities(List<MunicipalityDto> municipalities) {
        this.municipalities = municipalities;
    }
}