package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import nl.hva.ict.sm3.backend.dto.ForumQuestionDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a forum post or comment.
 *
 * <p>This entity is self-referencing: a ForumQuestion can have a parent question
 * (for comments) or be a top-level question (parent is null).
 * It also maintains a list of comments (children).
 *
 * <p>Fields include:
 * - body: content of the question/comment
 * - author: the user who posted it
 * - createdAt: timestamp
 * - question: parent question (null for top-level)
 * - comments: list of child comments
 * - createdAt: timestamp
 * - author: the user who posted
 * JPA annotations handle how this entity is stored in the database
 * and how related data is loaded.
 * JSON serialization is configured to prevent infinite loops.
 */

@Entity
@Table(name = "forum_questions") // in database

public class ForumQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Unique identifier for each forum question or comment
    private Long id;

    /**
     * The text content of the forum question or comment.
     * Cannot be blank and limited to 10000 characters.
     */

    @NotBlank
    @Column(nullable = false, length = 10000)
    private String body;

    /**
     * The parent question of this comment.
     * Null if this is a top-level question.
     *
     * <p>Ignored in JSON to prevent infinite recursion.
     */

    // Parent question (null for top-level questions, non-null for comments)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "question_id", nullable = true, columnDefinition = "BIGINT")
    @JsonIgnore // avoids JSON recursion
    private ForumQuestion question;

    /**
     * List of comments that belong to this question.
     *
     * Each comment refers to this question as its parent.
     * When the question is deleted, its comments are deleted as well.
     */


    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumQuestion> comments = new ArrayList<>();

     /**
     * The date and time when this question or comment was created.
     * This value is set automatically when the entity is saved.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * The user who created this question or comment.
     *
     * Every question or comment must have an author (ManyToOne relationship).
     */
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

    public ForumQuestionDto getPost() {
        return null;
    }
}
