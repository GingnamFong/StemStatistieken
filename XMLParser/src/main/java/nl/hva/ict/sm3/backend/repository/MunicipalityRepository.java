package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Municipality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MunicipalityRepository extends JpaRepository<Municipality, String> {
    
    @Query("SELECT m FROM Municipality m LEFT JOIN FETCH m.pollingStations WHERE m.id = :id")
    Optional<Municipality> findByIdWithPollingStations(@Param("id") String id);
    
    @Query("SELECT m FROM Municipality m WHERE m.id IN " +
           "(SELECT DISTINCT m2.id FROM Constituency c JOIN c.municipalities m2 WHERE c.id IN " +
           "(SELECT DISTINCT c3.id FROM Election e JOIN e.constituencies c3 WHERE e.id = :electionId))")
    List<Municipality> findByElectionId(@Param("electionId") String electionId);
    
    @Query("SELECT m FROM Municipality m WHERE m.id IN " +
           "(SELECT DISTINCT m2.id FROM Constituency c JOIN c.municipalities m2 WHERE c.id = :constituencyId)")
    List<Municipality> findByConstituencyId(@Param("constituencyId") String constituencyId);
}