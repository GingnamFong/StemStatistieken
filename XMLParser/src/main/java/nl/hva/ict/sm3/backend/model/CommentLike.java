package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a "like" on a forum comment.
 *
 * <p>In Path A, comments are stored as {@link ForumQuestion} rows with a non-null parent
 * (i.e., {@code question_id != null}).
 *
 * <p>A {@code CommentLike} links a {@link User} to a {@link ForumQuestion} (the comment),
 * indicating that the user has liked that comment.
 *
 * <p>Each user can like a specific comment only once, enforced by a database-level
 * unique constraint.
 *
 * <p>Indexes are added to optimize lookups by comment and by user.
 */
@Entity
@Table(
        name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_like_user_comment",
                columnNames = {"user_id", "comment_id"}
        ),
        indexes = {
                @Index(name = "idx_like_comment", columnList = "comment_id"),
                @Index(name = "idx_like_user", columnList = "user_id")
        }
)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who liked the comment.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The liked comment (stored as a ForumQuestion with a parent).
     *
     * <p>Column name remains "comment_id" but it references {@code forum_questions.id}.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private ForumQuestion comment;

    /**
     * Timestamp indicating when the like was created.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public CommentLike() {}

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ForumQuestion getComment() { return comment; }
    public void setComment(ForumQuestion comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
