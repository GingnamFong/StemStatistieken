package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByUserIdAndForumQuestionId(Long userId, Long forumQuestionId);
    long countByForumQuestionId(Long forumQuestionId);
    void deleteByUserIdAndForumQuestionId(Long userId, Long forumQuestionId);
}
