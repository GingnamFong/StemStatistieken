package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.Election;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ElectionRepository extends JpaRepository<Election, String> {
    
    /**
     * Find election by ID with all related entities eagerly loaded.
     * Uses multiple queries to avoid MultipleBagFetchException.
     */
    @Query("SELECT e FROM Election e WHERE e.id = :id")
    Optional<Election> findByIdWithDetails(@Param("id") String id);
    
    /**
     * Find all elections (basic info only).
     */
    @Query("SELECT e FROM Election e")
    List<Election> findAllElections();
    
    /**
     * Find all elections with pagination support.
     * 
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of elections
     */
    @Query("SELECT e FROM Election e")
    Page<Election> findAllElections(Pageable pageable);
    
    /**
     * Check if election exists by ID.
     */
    boolean existsById(String id);
}
