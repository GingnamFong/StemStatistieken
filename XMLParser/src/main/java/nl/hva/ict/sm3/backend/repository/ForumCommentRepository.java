package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    List<ForumComment> findByPostIdOrderByCreatedAtAsc(Long postId);
    @Query("""
        select c from ForumComment c
        join fetch c.post p
        join fetch p.author
        where c.id = :commentId
    """)
    Optional<ForumComment> findByIdWithPostAuthor(@Param("commentId") Long commentId);
}
