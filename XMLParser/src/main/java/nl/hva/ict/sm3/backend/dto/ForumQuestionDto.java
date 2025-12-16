package nl.hva.ict.sm3.backend.dto;

import jakarta.validation.constraints.NotBlank;
import nl.hva.ict.sm3.backend.model.ForumQuestion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for ForumQuestion - used for both input (POST) and output (GET) requests.
 * For input: only 'body' is required.
 * For output: includes id, body, createdAt, author, and comments.
 */
public class ForumQuestionDto {
    // For input (POST requests)
    @NotBlank
    private String body;

    // For output (GET requests) - these are set when converting from ForumQuestion
    private Long id;
    private LocalDateTime createdAt;
    private AuthorDto author;
    private List<ForumQuestionDto> comments;

    public ForumQuestionDto() {}

    // Getters and setters for input
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    // Getters and setters for output
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public List<ForumQuestionDto> getComments() {
        return comments;
    }

    public void setComments(List<ForumQuestionDto> comments) {
        this.comments = comments;
    }

    /**
     * Static factory method to convert ForumQuestion to DTO for responses.
     * This extracts only safe author information (no password).
     */
    public static ForumQuestionDto from(ForumQuestion question) {
        ForumQuestionDto dto = new ForumQuestionDto();
        dto.setId(question.getId());
        dto.setBody(question.getBody());
        dto.setCreatedAt(question.getCreatedAt());

        // Convert author - only safe fields, no password
        if (question.getAuthor() != null) {
            dto.setAuthor(new AuthorDto(
                question.getAuthor().getId(),
                question.getAuthor().getName(),
                question.getAuthor().getLastName(),
                question.getAuthor().getEmail()
            ));
        }

        // Convert comments recursively
        if (question.getComments() != null) {
            List<ForumQuestionDto> commentDtos = question.getComments().stream()
                .map(ForumQuestionDto::from)
                .collect(Collectors.toList());
            dto.setComments(commentDtos);
        }

        return dto;
    }

    /**
     * Inner DTO for author information (only safe fields, no password)
     */
    public static class AuthorDto {
        private Long id;
        private String name;
        private String lastName;
        private String email;

        public AuthorDto() {}

        public AuthorDto(Long id, String name, String lastName, String email) {
            this.id = id;
            this.name = name;
            this.lastName = lastName;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
