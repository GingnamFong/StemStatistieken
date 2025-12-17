package nl.hva.ict.sm3.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hva.ict.sm3.backend.dto.ForumQuestionDto;
import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.ForumQuestionRepository;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ForumQuestionController.
 *
 * <p>These tests verify that the REST endpoints work correctly:
 * <ul>
 *   <li>Creating a new forum question (POST /api/forum/questions)</li>
 *   <li>Retrieving all top-level questions (GET /api/forum/questions)</li>
 *   <li>Retrieving a specific question with comments (GET /api/forum/questions/{id})</li>
 *   <li>Creating a comment on a question (POST /api/forum/{questionId}/questions)</li>
 *   <li>Authentication and authorization checks</li>
 *   <li>Error handling and proper response format</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ForumQuestion Controller Tests")
class ForumQuestionControllerTest {

    @Mock
    private ForumQuestionRepository forumQuestionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ForumQuestionController forumQuestionController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private User testUser;
    private ForumQuestion savedQuestion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(forumQuestionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime support

        // Makes a test user
        testUser = new User("Gert", null, "gert@hotmail.com", "password123");
        testUser.setId(1L);

        savedQuestion = new ForumQuestion();
        savedQuestion.setId(1L);
        savedQuestion.setBody("Wat is de beste partij voor het milieu?");
        savedQuestion.setAuthor(testUser);
        savedQuestion.setQuestion(null); // Top-level question

        // Reset SecurityContext for each test
        SecurityContextHolder.clearContext();
    }

    /**
     * Tests that an authenticated user can successfully create a top-level forum question.
     * Verifies that the question is saved with the correct author and returns 201 Created.
     */
    @Test
    @DisplayName("A top-level question can be created via POST endpoint")
    void testCreateTopLevelQuestion() throws Exception {
        // Arrange: Set up authenticated user and mock repositories
        String userEmail = "gert@hotmail.com";

        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock Authentication - user is logged in
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);

        // Mock UserRepository - user exists in database
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // Mock ForumQuestionRepository - when save is called, return the saved question with ID
        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion question = invocation.getArgument(0);
            // Simulate the repository setting the ID (createdAt is automatically set by @PrePersist)
            question.setId(1L);
            return question;
        });

        // Create DTO for request body
        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("Wat is de beste partij voor het milieu?");

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.body").value("Wat is de beste partij voor het milieu?"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.email").value("gert@hotmail.com"))
                .andExpect(jsonPath("$.author.name").value("Gert"));

        // Verify: Check if repositories have been called correctly
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
    }

    /**
     * Tests that an unauthenticated user cannot create a question.
     * Verifies that the endpoint returns 401 Unauthorized when no user is logged in.
     */
    @Test
    @DisplayName("Creating a question without authentication returns 401 Unauthorized")
    void testCreateTopLevelQuestion_Unauthorized() throws Exception {
        // Arrange: No authentication set up (user is not logged in)
        SecurityContextHolder.clearContext();

        // Create DTO for request body
        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("Wat is de beste partij voor het milieu?");

        // Act & Assert: Perform POST request and verify 401 response
        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized

        // Verify: Repositories should not be called when unauthorized
        verify(userRepository, never()).findByEmail(anyString());
        verify(forumQuestionRepository, never()).save(any(ForumQuestion.class));
    }

    /**
     * Tests that all top-level questions can be retrieved successfully.
     * Verifies that the endpoint returns a list of questions without a parent.
     */
    @Test
    @DisplayName("All top-level questions can be retrieved via GET endpoint")
    void testGetAllTopLevelQuestions() throws Exception {
        // Arrange: Create a list of top-level questions
        List<ForumQuestion> questions = new ArrayList<>();
        
        ForumQuestion question1 = new ForumQuestion();
        question1.setId(1L);
        question1.setBody("Vraag 1");
        question1.setAuthor(testUser);
        question1.setQuestion(null);
        questions.add(question1);

        ForumQuestion question2 = new ForumQuestion();
        question2.setId(2L);
        question2.setBody("Vraag 2");
        question2.setAuthor(testUser);
        question2.setQuestion(null);
        questions.add(question2);

        // Mock repository to return the list of questions
        when(forumQuestionRepository.findAllTopLevelQuestions()).thenReturn(questions);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/forum/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].body").value("Vraag 1"))
                .andExpect(jsonPath("$[1].body").value("Vraag 2"));

        // Verify: Repository method was called
        verify(forumQuestionRepository, times(1)).findAllTopLevelQuestions();
    }

    /**
     * Tests that a specific question with its comments can be retrieved.
     * Verifies that the endpoint returns the question and loads its comments.
     */
    @Test
    @DisplayName("A specific question with comments can be retrieved via GET endpoint")
    void testGetQuestionById() throws Exception {
        // Arrange: Create a question with comments
        Long questionId = 1L;
        
        ForumQuestion question = new ForumQuestion();
        question.setId(questionId);
        question.setBody("Hoofdvraag");
        question.setAuthor(testUser);
        question.setQuestion(null);

        // Create comments for the question
        List<ForumQuestion> comments = new ArrayList<>();
        
        ForumQuestion comment1 = new ForumQuestion();
        comment1.setId(2L);
        comment1.setBody("Eerste reactie");
        comment1.setQuestion(question);
        comment1.setAuthor(testUser);
        comments.add(comment1);

        ForumQuestion comment2 = new ForumQuestion();
        comment2.setId(3L);
        comment2.setBody("Tweede reactie");
        comment2.setQuestion(question);
        comment2.setAuthor(testUser);
        comments.add(comment2);

        // Mock repository methods
        when(forumQuestionRepository.findByIdWithAuthor(questionId)).thenReturn(Optional.of(question));
        when(forumQuestionRepository.findByQuestionIdOrderByCreatedAtAsc(questionId)).thenReturn(comments);

        // Act & Assert: Perform GET request and verify response
        mockMvc.perform(get("/api/forum/questions/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(questionId))
                .andExpect(jsonPath("$.body").value("Hoofdvraag"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2));

        // Verify: Repository methods were called
        verify(forumQuestionRepository, times(1)).findByIdWithAuthor(questionId);
        verify(forumQuestionRepository, times(1)).findByQuestionIdOrderByCreatedAtAsc(questionId);
    }

    /**
     * Tests that retrieving a non-existent question returns 404 Not Found.
     */
    @Test
    @DisplayName("Retrieving a non-existent question returns 404 Not Found")
    void testGetQuestionById_NotFound() throws Exception {
        // Arrange: Question does not exist
        Long questionId = 999L;
        when(forumQuestionRepository.findByIdWithAuthor(questionId)).thenReturn(Optional.empty());

        // Act & Assert: Perform GET request and verify 404 response
        mockMvc.perform(get("/api/forum/questions/{questionId}", questionId))
                .andExpect(status().isNotFound());

        // Verify: Repository method was called
        verify(forumQuestionRepository, times(1)).findByIdWithAuthor(questionId);
        verify(forumQuestionRepository, never()).findByQuestionIdOrderByCreatedAtAsc(anyLong());
    }

    /**
     * Tests that an authenticated user can successfully create a comment on a question.
     * Verifies that the comment is saved with the correct parent question and author.
     */
    @Test
    @DisplayName("A comment can be created on a question via POST endpoint")
    void testCreateComment() throws Exception {
        // Arrange: Set up authenticated user and parent question
        String userEmail = "gert@hotmail.com";
        Long parentQuestionId = 1L;

        // Create parent question
        ForumQuestion parentQuestion = new ForumQuestion();
        parentQuestion.setId(parentQuestionId);
        parentQuestion.setBody("Bovenliggende vraag");
        parentQuestion.setAuthor(testUser);
        parentQuestion.setQuestion(null);

        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock Authentication - user is logged in
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);

        // Mock repositories
        when(forumQuestionRepository.findById(parentQuestionId)).thenReturn(Optional.of(parentQuestion));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        
        // Mock save to return comment with ID
        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion comment = invocation.getArgument(0);
            comment.setId(2L);
            return comment;
        });

        // Create DTO for comment request
        ForumQuestionDto commentDto = new ForumQuestionDto();
        commentDto.setBody("Dit is een reactie");

        // Act & Assert: Perform POST request and verify response
        mockMvc.perform(post("/api/forum/{questionId}/questions", parentQuestionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.body").value("Dit is een reactie"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.email").value("gert@hotmail.com"));

        // Verify: Repositories were called correctly
        verify(forumQuestionRepository, times(1)).findById(parentQuestionId);
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
    }

    /**
     * Tests that creating a comment on a non-existent question returns 404 Not Found.
     */
    @Test
    @DisplayName("Creating a comment on a non-existent question returns 404 Not Found")
    void testCreateComment_ParentNotFound() throws Exception {
        // Arrange: Set up authenticated user but parent question does not exist
        String userEmail = "gert@hotmail.com";
        Long nonExistentQuestionId = 999L;

        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);

        // Mock repository - parent question not found
        when(forumQuestionRepository.findById(nonExistentQuestionId)).thenReturn(Optional.empty());

        // Create DTO for comment request
        ForumQuestionDto commentDto = new ForumQuestionDto();
        commentDto.setBody("Dit is een reactie");

        // Act & Assert: Perform POST request and verify 404 response
        mockMvc.perform(post("/api/forum/{questionId}/questions", nonExistentQuestionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isNotFound());

        // Verify: Repository was called but save should not be called
        verify(forumQuestionRepository, times(1)).findById(nonExistentQuestionId);
        verify(forumQuestionRepository, never()).save(any(ForumQuestion.class));
        verify(userRepository, never()).findByEmail(anyString());
    }
}
