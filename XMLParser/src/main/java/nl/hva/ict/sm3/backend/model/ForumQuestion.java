package nl.hva.ict.sm3.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "forum_questions", indexes = {
        @Index(name = "idx_forum_question_user_created", columnList = "user_id, createdAt")
})

public class ForumQuestion {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
