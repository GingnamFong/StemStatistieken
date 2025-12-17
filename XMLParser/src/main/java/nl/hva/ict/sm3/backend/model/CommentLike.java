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
                columnNames = {"user_id", "forum_question_id"}
        ),
        indexes = {
                @Index(name = "idx_like_forum_question", columnList = "forum_question_id"),
                @Index(name = "idx_like_user", columnList = "user_id")
        }
)
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // This points to FORUM_QUESTIONS (your comments are there)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forum_question_id", nullable = false)
    private ForumQuestion forumQuestion;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CommentLike() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ForumQuestion getForumQuestion() { return forumQuestion; }
    public void setForumQuestion(ForumQuestion forumQuestion) { this.forumQuestion = forumQuestion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}

