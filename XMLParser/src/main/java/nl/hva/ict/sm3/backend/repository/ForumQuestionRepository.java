package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumQuestionRepository extends JpaRepository<ForumQuestion, Long> {
    List<ForumQuestion> findAllByOrderByCreatedAtAsc();

    List<ForumQuestion> findByAuthorIdOrderByCreatedAtAsc(Long authorId);

    @Query("""
    select q from ForumQuestion q
    join fetch q.author
    where q.id = :questionId
""")
    Optional<ForumQuestion> findByIdWithAuthor(@Param("questionId") Long Id);


}
