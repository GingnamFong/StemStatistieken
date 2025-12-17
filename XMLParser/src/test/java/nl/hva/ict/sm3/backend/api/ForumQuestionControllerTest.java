package nl.hva.ict.sm3.backend.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.hva.ict.sm3.backend.dto.ForumQuestionDto;
import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.ForumQuestionRepository;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ForumQuestionController.
 *
 * Test categories:
 * - Happy flows: normal, expected input
 * - Invalid input: incorrect parameters, incorrect values, or missing data
 * - Error handling: does the code throw the correct exception, or does the API return the correct status code?
 * - Business rules: are all rules and validations implemented correctly?
 */
@ExtendWith(MockitoExtension.class)
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

        // Test data
        testUser = new User("Gert", null, "gert@hotmail.com", "password123");
        testUser.setId(1L);

        savedQuestion = new ForumQuestion();
        savedQuestion.setId(1L);
        savedQuestion.setBody("What is the best party for the environment?");
        savedQuestion.setAuthor(testUser);
        savedQuestion.setQuestion(null); // Top-level question

        // Reset SecurityContext for each test
        SecurityContextHolder.clearContext();
    }

    // HAPPY FLOW - normal, expected input

    @Test
    void testCreateTopLevelQuestion_HappyFlow() throws Exception {
        // Arrange: Mock authentication and repositories
        String userEmail = "gert@hotmail.com";

        // Mock SecurityContext and Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock Authentication - user is logged in
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);

        // Mock UserRepository - user exists
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        // Mock ForumQuestionRepository - when save is called, return the saved question
        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion question = invocation.getArgument(0);
            // Simulate the repository setting the ID (createdAt is automatically set by @PrePersist)
            question.setId(1L);
            return question;
        });

        // Create DTO for request
        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("What is the best party for the environment?");

        // Act & Assert: POST request to /api/forum/questions
        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.body").value("What is the best party for the environment?"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.email").value("gert@hotmail.com"))
                .andExpect(jsonPath("$.author.name").value("Gert"));

        // Verify: check if repositories have been called
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
        
        // Success message
        System.out.println("✓ Test passed: Creating a forum question works correctly!");
        System.out.println("  - Status code: 201 Created");
        System.out.println("  - Question saved with ID: 1");
        System.out.println("  - Author information (Gert, gert@hotmail.com) is correctly returned");
    }
}
