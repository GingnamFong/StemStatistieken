package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
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

    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnore // avoids JSON recursion
    private ForumQuestion question;

     */

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumQuestion> comments;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User author;

    @PrePersist
    void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ForumQuestion() {}

    public Long getId() { return id; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    /*
    public ForumQuestion getQuestion() { return question; }
    public void setQuestion(ForumQuestion question) { this.question = question; }

     */

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) {
        this.id = id;
    }



}
