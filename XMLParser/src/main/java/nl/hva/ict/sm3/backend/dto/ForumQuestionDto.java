package nl.hva.ict.sm3.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ForumQuestionDto {
    public String getBody() {

        return "";
    }

    @NotBlank
    private String body;

    public ForumQuestionDto() {

    }

    public void setBody(String body) {this.body = body;}
}
