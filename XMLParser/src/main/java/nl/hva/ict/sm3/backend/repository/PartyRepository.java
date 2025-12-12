package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {
    
    @Query("SELECT p FROM Party p WHERE p.id IN " +
           "(SELECT DISTINCT p2.id FROM Election e JOIN e.parties p2 WHERE e.id = :electionId) " +
           "ORDER BY p.votes DESC")
    List<Party> findByElectionId(@Param("electionId") String electionId);
    
    @Query("SELECT p FROM Party p WHERE p.id IN " +
           "(SELECT DISTINCT p2.id FROM Election e JOIN e.parties p2 WHERE e.id = :electionId) " +
           "ORDER BY p.votes DESC")
    List<Party> findTopPartiesByElectionId(@Param("electionId") String electionId);
}