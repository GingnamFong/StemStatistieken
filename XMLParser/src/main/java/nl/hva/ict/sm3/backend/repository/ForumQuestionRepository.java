package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for accessing and managing ForumQuestion entities.
 * Provides standard CRUD operations and custom queries
 * for forum-related data.
<br>
 * Retrieves all child questions (comments) for a given parent question,
 * ordered by creation time ascending.
 */
public interface ForumQuestionRepository extends JpaRepository<ForumQuestion, Long> {
    // All comments (child questions) for a given parent question, ordered by creation time
    List<ForumQuestion> findByQuestionIdOrderByCreatedAtAsc(Long questionId);

    List<ForumQuestion> findByAuthorIdOrderByCreatedAtAsc(Long authorId);

    // Find specific question data including the author
    @Query("""
    select q from ForumQuestion q
    join fetch q.author
    where q.id = :questionId
""")
    // Find comments from a specific parent question
    Optional<ForumQuestion> findByIdWithAuthor(@Param("questionId") Long questionId);
    @Query("""
  select c from ForumQuestion c
  join fetch c.author
  where c.question.id = :questionId
  order by c.createdAt asc
""")
    List<ForumQuestion> findCommentsWithAuthor(@Param("questionId") Long questionId);
    // Find all top-level questions (posts without a parent question)
    @Query("""
    select distinct q from ForumQuestion q
    left join fetch q.author
    left join fetch q.comments c
    left join fetch c.author
    where q.question is null
    order by q.createdAt desc
""")
    List<ForumQuestion> findAllTopLevelQuestions();

}
