package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.PollingStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PollingStationRepository extends JpaRepository<PollingStation, String> {
    
    @Query("SELECT ps FROM PollingStation ps WHERE ps.id IN " +
           "(SELECT DISTINCT ps2.id FROM Municipality m JOIN m.pollingStations ps2 WHERE m.id = :municipalityId)")
    List<PollingStation> findByMunicipalityId(@Param("municipalityId") String municipalityId);
    
    @Query("SELECT ps FROM PollingStation ps WHERE ps.postalCode BETWEEN :startPostalCode AND :endPostalCode")
    List<PollingStation> findByPostalCodeRange(@Param("startPostalCode") String startPostalCode,
                                               @Param("endPostalCode") String endPostalCode);
    
    @Query("SELECT ps FROM PollingStation ps WHERE ps.id IN " +
           "(SELECT DISTINCT ps2.id FROM Municipality m JOIN m.pollingStations ps2 WHERE m.id IN " +
           "(SELECT DISTINCT m3.id FROM Constituency c JOIN c.municipalities m3 WHERE c.id IN " +
           "(SELECT DISTINCT c4.id FROM Election e JOIN e.constituencies c4 WHERE e.id = :electionId))) " +
           "AND ps.postalCode BETWEEN :startPostalCode AND :endPostalCode")
    List<PollingStation> findByElectionIdAndPostalCodeRange(@Param("electionId") String electionId,
                                                           @Param("startPostalCode") String startPostalCode,
                                                           @Param("endPostalCode") String endPostalCode);
}