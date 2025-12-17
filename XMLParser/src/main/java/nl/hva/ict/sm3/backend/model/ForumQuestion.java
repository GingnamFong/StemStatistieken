package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_questions")

public class ForumQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 10000)
    private String body;

    // Parent question (null for top-level questions, non-null for comments)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "question_id", nullable = true, columnDefinition = "BIGINT")
    @JsonIgnore // avoids JSON recursion
    private ForumQuestion question;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumQuestion> comments = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ForumQuestion() {}

    public Long getId() { return id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public ForumQuestion getQuestion() { return question; }
    public void setQuestion(ForumQuestion question) { this.question = question; }

    public List<ForumQuestion> getComments() { return comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
}
