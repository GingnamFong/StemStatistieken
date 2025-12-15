package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link ForumComment} persistence and retrieval.
 *
 * <p>Contains derived queries for common use cases and a custom JPQL query
 * to efficiently load related entities needed for authorization checks.
 */
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    /**
     * Retrieves all comments for a specific forum post ordered by creation time ascending.
     *
     * <p>This method is typically used to render a comment thread in chronological order.
     *
     * @param postId the ID of the forum post
     * @return list of comments for the given post, sorted by {@code createdAt} ascending
     */
    List<ForumComment> findByPostIdOrderByCreatedAtAsc(Long postId);
    /**
     * Retrieves a comment by ID and eagerly fetches the associated post and post author.
     *
     * <p>This query is designed for authorization checks, where we need to know
     * who owns the related {@code ForumPost} (e.g., only the post author may delete comments).
     *
     * <p>Using {@code join fetch} avoids lazy-loading/N+1 query issues when accessing:
     * <ul>
     *   <li>{@code comment.post}</li>
     *   <li>{@code comment.post.author}</li>
     * </ul>
     *
     * @param commentId the ID of the comment
     * @return an {@link Optional} containing the comment if found
     */

    @Query("""
        select c from ForumComment c
        join fetch c.post p
        join fetch p.author
        where c.id = :commentId
    """)
    Optional<ForumComment> findByIdWithPostAuthor(@Param("commentId") Long commentId);
}
