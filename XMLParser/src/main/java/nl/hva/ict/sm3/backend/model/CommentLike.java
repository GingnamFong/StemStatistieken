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

    /**
     * Primary key of the comment like.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who liked the comment.
     *
     * <p>This association is mandatory; a like cannot exist without a user.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    /**
     * The forum comment that was liked.
     *
     * <p>This field references a {@link ForumQuestion} that represents a comment,
     * not a top-level forum question. Validation to ensure this is a comment
     * (i.e. {@code question != null}) is handled in the service or controller layer.
     */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "forum_question_id", nullable = false)
    private ForumQuestion forumQuestion;

    /**
     * Timestamp indicating when the like was created.
     *
     * <p>This value is automatically initialized when the entity is created.
     */

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Default constructor required by JPA.
     */

    public CommentLike() {}

    /**
     * Returns the unique identifier of this comment like.
     *
     * @return the ID of the comment like
     */

    public Long getId() { return id; }

    /**
     * Returns the user who liked the comment.
     *
     * @return the liking user
     */
    public User getUser() { return user; }

    /**
     * Sets the user who liked the comment.
     *
     * @param user the user performing the like
     */
    public void setUser(User user) { this.user = user; }

    /**
     * Returns the forum comment that was liked.
     *
     * @return the liked forum comment
     */
    public ForumQuestion getForumQuestion() { return forumQuestion; }

    /**
     * Sets the forum comment that was liked.
     *
     * @param forumQuestion the forum comment
     */

    public void setForumQuestion(ForumQuestion forumQuestion) { this.forumQuestion = forumQuestion; }

    /**
     * Returns the timestamp when the like was created.
     *
     * @return creation timestamp
     */

    public LocalDateTime getCreatedAt() { return createdAt; }
}

