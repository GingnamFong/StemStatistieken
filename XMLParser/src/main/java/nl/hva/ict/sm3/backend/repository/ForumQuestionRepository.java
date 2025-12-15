package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumQuestionRepository extends JpaRepository<ForumQuestion, Long> {

}
