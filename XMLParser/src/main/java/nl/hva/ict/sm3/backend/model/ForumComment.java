package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_comments")
public class ForumComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 5000)
    private String body;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore // voorkomt JSON recursion / huge payload
    private ForumPost post;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User author;


    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public ForumComment() {}

    public Long getId() { return id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public ForumPost getPost() { return post; }
    public void setPost(ForumPost post) { this.post = post; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
