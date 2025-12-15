package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumQuestionRepository extends JpaRepository<ForumQuestion, Long> {
    List<ForumQuestion> findByQuestionIdOrderByCreatedAtAsc(Long questionId);
    @Query("""
        select c from ForumQuestion c
        join fetch c.question p
        join fetch p.author
        where c.id = :questionId
    """)
    Optional<ForumQuestion> findByIdWithQuestionAuthor(@Param("questionId") Long questionId);


}
