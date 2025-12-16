package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;


/**
 * Entity representing a comment on a forum post.
 *
 * <p>A {@code ForumComment} is created by a {@link User} and belongs to a
 * specific {@link ForumPost}. Each comment contains textual content and
 * a creation timestamp.
 *
 * <p>Relationships are configured with {@link FetchType#LAZY} to prevent
 * unnecessary database loading and improve performance.
 */
@Entity
@Table(name = "forum_comments")
public class ForumComment {
    /**
     * Primary key of the forum comment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * The textual content of the comment.
     *
     * <p>This field is mandatory and limited to 5000 characters.
     */
    @NotBlank
    @Column(nullable = false, length = 5000)
    private String body;
    /**
     * The forum post to which this comment belongs.
     *
     * <p>This relationship is mandatory and loaded lazily.
     * {@link JsonIgnore} is used to prevent infinite JSON recursion
     * during serialization.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore // voorkomt JSON recursion / huge payload
    private ForumPost post;
    /**
     * Timestamp indicating when the comment was created.
     *
     * <p>This value is automatically set when the entity is persisted.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    /**
     * The user who authored this comment.
     *
     * <p>This relationship is mandatory and loaded lazily.
     * {@link JsonIgnore} prevents exposing user details in API responses.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User author;

    /**
     * Lifecycle callback that sets the creation timestamp
     * just before the entity is persisted.
     */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Default constructor required by JPA.
     */
    public ForumComment() {}

    /**
     * Returns the unique identifier of the comment.
     *
     * @return the comment ID
     */
    public Long getId() { return id; }


    /**
     * Returns the author of the comment.
     *
     * @return the {@link User} who created the comment
     */
    public User getAuthor() { return author; }

    /**
     * Sets the author of the comment.
     *
     * @param author the user who created the comment
     */
    public void setAuthor(User author) { this.author = author; }


    /**
     * Returns the textual content of the comment.
     *
     * @return the comment body
     */
    public String getBody() { return body; }

    /**
     * Sets the textual content of the comment.
     *
     * @param body the comment body
     */
    public void setBody(String body) { this.body = body; }

    /**
     * Returns the forum post this comment belongs to.
     *
     * @return the related {@link ForumPost}
     */
    public ForumPost getPost() { return post; }

    /**
     * Sets the forum post this comment belongs to.
     *
     * @param post the related forum post
     */
    public void setPost(ForumPost post) { this.post = post; }

    /**
     * Returns the creation timestamp of the comment.
     *
     * @return the creation date and time
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}
