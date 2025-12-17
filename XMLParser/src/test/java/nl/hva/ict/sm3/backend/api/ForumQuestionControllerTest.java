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
 * Unit tests voor ForumQuestionController.
 * 
 * Test categorieën:
 * - Happy flows: normale, verwachte input
 * - Invalid input: verkeerde parameters, foutieve waarden, of ontbrekende data
 * - Foutafhandeling: werpt de code de juiste exception, of geeft de API de juiste statuscode terug?
 * - Business rules: zijn alle regels en validaties correct geïmplementeerd
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
        objectMapper.findAndRegisterModules(); // Voor LocalDateTime support

        // Test data
        testUser = new User("Jan", "Jansen", "jan@example.com", "password123");
        testUser.setId(1L);

        savedQuestion = new ForumQuestion();
        savedQuestion.setId(1L);
        savedQuestion.setBody("Wat is de beste partij voor het milieu?");
        savedQuestion.setAuthor(testUser);
        savedQuestion.setQuestion(null); // Top-level question

        // Reset SecurityContext voor elke test
        SecurityContextHolder.clearContext();
    }

    // HAPPY FLOW - normale, verwachte input

    @Test
    void testCreateTopLevelQuestion_HappyFlow() throws Exception {
        // Arrange: Mock authentication en repositories
        String userEmail = "jan@example.com";
        
        // Mock SecurityContext en Authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        // Mock Authentication - gebruiker is ingelogd
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        
        // Mock UserRepository - gebruiker bestaat
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));
        
        // Mock ForumQuestionRepository - wanneer save wordt aangeroepen, geef de opgeslagen vraag terug
        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion question = invocation.getArgument(0);
            // Simuleer dat de repository het ID zet (createdAt wordt automatisch gezet door @PrePersist)
            question.setId(1L);
            return question;
        });

        // Maak DTO voor de request
        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("Wat is de beste partij voor het milieu?");

        // Act & Assert: POST request naar /api/forum/questions
        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated()) // 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.body").value("Wat is de beste partij voor het milieu?"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.author.email").value("jan@example.com"))
                .andExpect(jsonPath("$.author.name").value("Jan"));

        // Verify: check of repositories zijn aangeroepen
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
    }
}
