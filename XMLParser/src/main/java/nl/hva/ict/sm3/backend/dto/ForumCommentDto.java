package nl.hva.ict.sm3.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for creating or updating a forum comment.
 *
 * <p>This DTO is used to receive input from the client when a user
 * submits a new comment to a forum post.
 *
 * <p>The {@code body} field is mandatory and may not be blank.
 */

public class ForumCommentDto {
    /**
     * The textual content of the forum comment.
     * This field must not be {@code null}, empty, or only whitespace.
     */
    @NotBlank
    private String body;
    /**
     * Default constructor required for JSON deserialization.
     */
    public ForumCommentDto() {
    }
    /**
     * Returns the comment text.
     *
     * @return the body of the comment
     */
    public String getBody() {
        return body;
    }
    /**
     * Sets the comment text.
     *
     * @param body the body of the comment
     */
    public void setBody(String body) {
        this.body = body;
    }
}
