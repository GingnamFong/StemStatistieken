package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


/**
 * Entity representing a "like" on a forum comment.
 *
 * <p>A {@code CommentLike} links a {@link User} to a {@link ForumComment},
 * indicating that the user has liked the comment.
 *
 * <p>Each user can like a specific comment only once, enforced by a
 * database-level unique constraint.
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

    /**
     * Primary key of the comment like.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The user who liked the comment.
     *
     * <p>This relationship is mandatory and loaded lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The comment that was liked.
     *
     * <p>This relationship is mandatory and loaded lazily.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private ForumComment comment;

    /**
     * Timestamp indicating when the like was created.
     *
     * <p>Defaults to the current date and time.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Default constructor required by JPA.
     */
    public CommentLike() {}

    /**
     * Returns the unique identifier of the like.
     *
     * @return the like ID
     */
    public Long getId() { return id; }

    /**
     * Returns the user who liked the comment.
     *
     * @return the {@link User} who created the like
     */
    public User getUser() { return user; }

    /**
     * Sets the user who liked the comment.
     *
     * @param user the user creating the like
     */
    public void setUser(User user) { this.user = user; }

    /**
     * Returns the comment that was liked.
     *
     * @return the liked {@link ForumComment}
     */
    public ForumComment getComment() { return comment; }

    /**
     * Sets the comment that is being liked.
     *
     * @param comment the forum comment
     */
    public void setComment(ForumComment comment) { this.comment = comment; }
    /**
     * Returns the timestamp when the like was created.
     *
     * @return the creation date and time
     */

    public LocalDateTime getCreatedAt() { return createdAt; }
}
