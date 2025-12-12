package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {
    
    @Query("SELECT c FROM Candidate c WHERE c.id IN " +
           "(SELECT DISTINCT c2.id FROM Election e JOIN e.candidates c2 WHERE e.id = :electionId)")
    List<Candidate> findByElectionId(@Param("electionId") String electionId);
    
    @Query("SELECT c FROM Candidate c WHERE c.id IN " +
           "(SELECT DISTINCT c2.id FROM Election e JOIN e.candidates c2 WHERE e.id = :electionId) " +
           "AND c.partyId = :partyId")
    List<Candidate> findByElectionIdAndPartyId(@Param("electionId") String electionId, 
                                               @Param("partyId") String partyId);
    
    Optional<Candidate> findByIdAndPartyId(String id, String partyId);
    
    @Query("SELECT c FROM Candidate c WHERE c.lastName = :lastName AND c.initials = :initials")
    Optional<Candidate> findByLastNameAndInitials(@Param("lastName") String lastName, 
                                                  @Param("initials") String initials);
}