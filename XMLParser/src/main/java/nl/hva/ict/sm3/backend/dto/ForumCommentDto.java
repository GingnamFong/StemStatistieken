package nl.hva.ict.sm3.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class ForumCommentDto {
    @NotBlank
    private String body;

    public ForumCommentDto() {
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
