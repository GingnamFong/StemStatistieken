package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * Repository interface for managing {@link CommentLike} entities.
 *
 * <p>This repository provides database access methods related to likes on forum comments.
 * It follows the Spring Data JPA pattern and contains only persistence-related logic.
 *
 * <p>Business rules (such as validating whether a forum post is a comment) are handled
 * in the service layer, not in this repository.
 *
 * <p>All methods use Spring Data JPA derived query mechanisms.
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    /**
     * Checks whether a specific user has already liked a given forum comment.
     *
     * <p>This method is typically used to prevent duplicate likes before creating
     * a new {@link CommentLike}.
     *
     * @param userId the ID of the user
     * @param forumQuestionId the ID of the forum comment
     * @return {@code true} if the user has already liked the comment, {@code false} otherwise
     */
    boolean existsByUserIdAndForumQuestionId(Long userId, Long forumQuestionId);
    /**
     * Counts the total number of likes for a given forum comment.
     *
     * <p>This method is commonly used to display the number of likes associated
     * with a comment.
     *
     * @param forumQuestionId the ID of the forum comment
     * @return the number of likes for the specified comment
     */
    long countByForumQuestionId(Long forumQuestionId);
    /**
     * Removes a like for a specific forum comment made by a specific user.
     *
     * <p>This method supports the "unlike" functionality and ensures that
     * only the targeted {@link CommentLike} is removed.
     *
     * @param userId the ID of the user
     * @param forumQuestionId the ID of the forum comment
     */
    void deleteByUserIdAndForumQuestionId(Long userId, Long forumQuestionId);
}
