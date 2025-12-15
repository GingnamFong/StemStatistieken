package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private ForumComment comment;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public CommentLike() {}

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public ForumComment getComment() { return comment; }
    public void setComment(ForumComment comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
