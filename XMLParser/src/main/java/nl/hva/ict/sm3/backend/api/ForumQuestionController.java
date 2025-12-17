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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/forum")

// gets questions, comments and users from the database
public class ForumQuestionController {
    private final ForumQuestionRepository forumQuestionRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public ForumQuestionController(ForumQuestionRepository forumQuestionRepository,
                                   UserRepository userRepository) {
        this.forumQuestionRepository = forumQuestionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all top-level forum questions (questions without a parent).
     *
     * @return list of ForumQuestionDto wrapped in ResponseEntity
     */

    // GET all top-level questions (posts without parent)
    @GetMapping("/questions")
    public ResponseEntity<List<ForumQuestionDto>> getAllTopLevelQuestions() {
        try { // Fetch all questions that do not have a parent question
            List<ForumQuestion> questions = forumQuestionRepository.findAllTopLevelQuestions();
            System.out.println("Found " + questions.size() + " top-level questions");
            // Convert entities to DTOS
            List<ForumQuestionDto> responseDtos = questions.stream()
                .map(question -> {
                    System.out.println("Processing question ID: " + question.getId() + ", Author: " + 
                        (question.getAuthor() != null ? question.getAuthor().getName() : "null"));
                    return ForumQuestionDto.from(question);
                })
                .collect(Collectors.toList());
            
            System.out.println("Returning " + responseDtos.size() + " DTOs");
            return ResponseEntity.ok(responseDtos);
        } catch (Exception e) {
            System.err.println("Error in getAllTopLevelQuestions: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // GET a specific question with its comments
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ForumQuestionDto> getQuestion(@PathVariable Long questionId) {
        Optional<ForumQuestion> questionOpt = forumQuestionRepository.findByIdWithAuthor(questionId);
        if (questionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ForumQuestion question = questionOpt.get();
        // Load comments
        List<ForumQuestion> comments = forumQuestionRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
        question.getComments().clear();
        question.getComments().addAll(comments);
        return ResponseEntity.ok(ForumQuestionDto.from(question));
    }

    // POST create a new top-level question (post). Only for log in users. Makes new main question
    @PostMapping("/questions")
    public ResponseEntity<ForumQuestionDto> createTopLevelQuestion(@Valid @RequestBody ForumQuestionDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) { // no parent
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ForumQuestion question = new ForumQuestion();
        question.setBody(dto.getBody());
        question.setQuestion(null); // Top-level question has no parent
        question.setAuthor(user);

        ForumQuestion saved = forumQuestionRepository.save(question);
        return ResponseEntity.status(201).body(ForumQuestionDto.from(saved));
    }

    // GET all comments for a specific question
    @GetMapping("/{questionId}/questions")
    public ResponseEntity<List<ForumQuestionDto>> getQuestionComments(@PathVariable("questionId") Long questionId) {
        if (!forumQuestionRepository.existsById(questionId)) {
            return ResponseEntity.notFound().build();
        }
        List<ForumQuestion> comments = forumQuestionRepository.findByQuestionIdOrderByCreatedAtAsc(questionId);
        List<ForumQuestionDto> responseDtos = comments.stream()
            .map(ForumQuestionDto::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // POST create a comment on a question
    @PostMapping("/{questionId}/questions")
    public ResponseEntity<ForumQuestionDto> addQuestion(@PathVariable Long questionId,
                                                     @Valid @RequestBody ForumQuestionDto dto) {
        ForumQuestion parentQuestion = forumQuestionRepository.findById(questionId).orElse(null);
        if (parentQuestion == null) {
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
        // link this new question as a comment to the parent question
        question.setQuestion(parentQuestion);
        question.setAuthor(user);

        ForumQuestion saved = forumQuestionRepository.save(question);
        return ResponseEntity.status(201).body(ForumQuestionDto.from(saved));
    }
    // Deleting a question
    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("questionId") Long questionId) {

        Optional<ForumQuestion> questionOpt  = forumQuestionRepository.findById(questionId);
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
