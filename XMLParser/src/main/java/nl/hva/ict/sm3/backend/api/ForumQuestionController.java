package nl.hva.ict.sm3.backend.api;

import jakarta.validation.Valid;
import nl.hva.ict.sm3.backend.dto.ForumQuestionDto;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.ForumQuestionRepository;
import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/forum")

public class ForumQuestionController {
    private final ForumQuestionRepository forumQuestionRepository;
    private final UserRepository userRepository;

    public ForumQuestionController(ForumQuestionRepository forumQuestionRepository,
                                   UserRepository userRepository) {
        this.forumQuestionRepository = forumQuestionRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/{questionId}/questions")
    public ResponseEntity<List<ForumQuestion>> getForumQuestions(@PathVariable("questionId") String questionId) {
        if (!forumQuestionRepository.existsById(Long.valueOf(questionId))) {
            return ResponseEntity.notFound().build();
        }
        List<ForumQuestion> questions = forumQuestionRepository.findAllByOrderByCreatedAtAsc(questionId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{questionId}/questions")
    public ResponseEntity<ForumQuestion> addQuestion(@PathVariable Long questionId,
                                                     @Valid @RequestBody ForumQuestionDto dto) {
        ForumQuestion forumQuestion = forumQuestionRepository.findById(questionId).orElse(null);
        if (forumQuestion == null) {
            return ResponseEntity.notFound().build();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ForumQuestion question = new ForumQuestion();
        question.setBody(dto.getBody());
        question.setQuestion(question);
        question.setAuthor(user);

        ForumQuestion saved =  forumQuestionRepository.save(question);
        return ResponseEntity.status(201).body(saved);
    }
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<ForumQuestion> deleteQuestion(@PathVariable("questionId") String questionId) {

        Optional<ForumQuestion> questionOpt  = forumQuestionRepository.findById(Long.valueOf(questionId));
        if (questionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ForumQuestion question = questionOpt.get();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
/*
        if (question.getPost() == null
                || question.getPost().getAuthor() == null
                || !question.getPost().getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

 */

        forumQuestionRepository.delete(question);
        return ResponseEntity.noContent().build();
    }

}
