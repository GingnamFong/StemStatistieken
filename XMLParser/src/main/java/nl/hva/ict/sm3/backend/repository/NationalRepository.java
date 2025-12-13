package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.National;
import nl.hva.ict.sm3.backend.model.NationalResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NationalRepository extends JpaRepository<National, String> {
    
    @Query("SELECT n FROM National n WHERE n.electionId = :electionId")
    List<National> findByElectionId(@Param("electionId") String electionId);
    
    @Query("SELECT n FROM National n WHERE n.electionId = :electionId AND n.type = :type")
    List<National> findByElectionIdAndType(@Param("electionId") String electionId, 
                                           @Param("type") NationalResult type);
    
    @Query("SELECT n FROM National n WHERE n.electionId = :electionId AND n.partyId = :partyId")
    List<National> findByElectionIdAndPartyId(@Param("electionId") String electionId, 
                                              @Param("partyId") String partyId);
}