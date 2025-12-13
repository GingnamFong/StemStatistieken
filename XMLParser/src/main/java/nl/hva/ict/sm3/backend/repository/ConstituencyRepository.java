package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Constituency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConstituencyRepository extends JpaRepository<Constituency, String> {
    
    @Query("SELECT c FROM Constituency c LEFT JOIN FETCH c.municipalities WHERE c.id = :id")
    Optional<Constituency> findByIdWithMunicipalities(@Param("id") String id);
    
    @Query("SELECT c FROM Constituency c WHERE c.id IN " +
           "(SELECT DISTINCT c2.id FROM Election e JOIN e.constituencies c2 WHERE e.id = :electionId)")
    List<Constituency> findByElectionId(@Param("electionId") String electionId);
    
    @Query("SELECT COUNT(c) FROM Constituency c WHERE c.id LIKE :electionIdPrefix%")
    long countByElectionIdPrefix(@Param("electionIdPrefix") String electionIdPrefix);
}