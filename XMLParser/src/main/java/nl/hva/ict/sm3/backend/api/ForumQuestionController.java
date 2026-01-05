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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller responsible for managing forum questions and comments.
 *
 * Supports:
 * - Retrieving top-level questions
 * - Retrieving a single question with its comments
 * - Creating questions and comments
 * - Deleting questions
 *
 * Authentication is handled via Spring Security (see http 401, if user == NULL).
 */


@RestController
@RequestMapping("/api/forum")

// For forum related API-endpoints
public class ForumQuestionController {
    private final ForumQuestionRepository forumQuestionRepository;
    private final UserRepository userRepository;

    public ForumQuestionController(ForumQuestionRepository forumQuestionRepository,
                                   UserRepository userRepository) {
        this.forumQuestionRepository = forumQuestionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all top-level forum questions (questions without a parent).
     *
     * <p>Each top-level question is converted to a ForumQuestionDto to avoid
     * exposing JPA entities directly and prevent lazy loading issues.
     *
     * @return a ResponseEntity containing a list of top-level ForumQuestionDto objects
     *         and HTTP status 200 if successful, or HTTP 500 if an error occurs
     */


    // GET all top-level questions (posts without parent), gets all the top questions and converts ForumQuestion into ForumQuestionDto
    @GetMapping("/questions")
    public ResponseEntity<List<ForumQuestionDto>> getAllTopLevelQuestions() {
        try {
            List<ForumQuestion> questions = forumQuestionRepository.findAllTopLevelQuestions(); // query
            System.out.println("Found " + questions.size() + " top-level questions");

            // avoids lazy loading
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

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", "Failed to retrieve forum questions");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body((List<ForumQuestionDto>) errorResponse);
        }
    }

    /**
     * Retrieves a specific forum question along with its comments.
     *
     * <p>The question and its comments are converted to DTOs to avoid lazy loading issues
     * and prevent recursive JSON serialization.
     *
     * @param questionId the ID of the question to retrieve
     * @return a ResponseEntity containing the question as a ForumQuestionDto with nested
     *         comments, HTTP 404 if the question does not exist
     */

    // GET a specific question with its comments
    @GetMapping("/questions/{questionId}")
    public ResponseEntity<ForumQuestionDto> getQuestion(@PathVariable Long questionId) {
        Optional<ForumQuestion> questionOpt = forumQuestionRepository.findByIdWithAuthor(questionId);
        if (questionOpt.isEmpty()) return ResponseEntity.notFound().build();

        ForumQuestion question = questionOpt.get();

        List<ForumQuestion> comments = forumQuestionRepository.findCommentsWithAuthor(questionId);

        ForumQuestionDto dto = ForumQuestionDto.from(question); // MUST NOT touch entity comments inside from()
        dto.setComments(comments.stream().map(ForumQuestionDto::from).toList());

        return ResponseEntity.ok(dto);
    }

    /**
     * Creates a new top-level forum question (no parent question).
     *
     * <p>Only authenticated users can create questions. The currently authenticated
     * user is set as the author of the new question.
     *
     * @param dto the ForumQuestionDto containing the body of the question
     * @return a ResponseEntity containing the created ForumQuestionDto and HTTP status 201,
     *         or HTTP 401 if the user is not authenticated
     */

    // POST create a new top-level question (post)
    @PostMapping("/questions")
    public ResponseEntity<ForumQuestionDto> createTopLevelQuestion(@Valid @RequestBody ForumQuestionDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 401 error if user is not authenticated
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "You must be logged in to create a question");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body((ForumQuestionDto) errorResponse);
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);

        // 401 error if user is not found
        if (user == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 401);
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Authenticated user not found");

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body((ForumQuestionDto) errorResponse);
        }

        ForumQuestion question = new ForumQuestion();
        question.setBody(dto.getBody());
        question.setQuestion(null); // Top-level question has no parent
        question.setAuthor(user);

        // 201 code when question is created successfully
        ForumQuestion saved = forumQuestionRepository.save(question);
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("status", 201);
        successResponse.put("message", "Question created successfully");
        successResponse.put("data", ForumQuestionDto.from(saved));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body((ForumQuestionDto) successResponse);
    }

    /**
     * Retrieves all comments for a given forum question, ordered by creation date.
     *
     * @param questionId the ID of the parent question
     * @return a ResponseEntity containing a list of ForumQuestionDto objects for each comment,
     *         HTTP 404 if the parent question does not exist
     */


    // GET all comments for a specific (top) question
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

    /**
     * Adds a new comment to an existing forum question.
     *
     * <p>Only authenticated users can add comments. The new comment references the
     * parent question and the currently authenticated user as the author.
     *
     * @param questionId the ID of the parent question
     * @param dto the ForumQuestionDto containing the comment body
     * @return a ResponseEntity containing the created ForumQuestionDto and HTTP status 201,
     *         or HTTP 404 if the parent question does not exist,
     *         or HTTP 401 if the user is not authenticated
     */

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

    /**
     * Deletes a forum question.
     *
     * <p>Only authenticated users can delete questions. Currently, the authorization
     * check to verify if the authenticated user is the author is commented out.
     *
     * @param questionId the ID of the question to delete
     * @return a ResponseEntity with HTTP status 204 if the deletion was successful,
     *         HTTP 404 if the question does not exist,
     *         or HTTP 401 if the user is not authenticated
     */

    @DeleteMapping("/questions/{questionId}") // Deletes question
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
