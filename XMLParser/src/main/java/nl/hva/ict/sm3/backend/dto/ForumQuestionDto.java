package nl.hva.ict.sm3.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ForumQuestionDto {
    @NotBlank
    private String body;

    public ForumQuestionDto() {

    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {this.body = body;}
}
