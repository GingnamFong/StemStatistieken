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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        testUser = new User("Gert", null, "gert@hotmail.com", "password123");
        testUser.setId(1L);

        savedQuestion = new ForumQuestion();
        savedQuestion.setId(1L);
        savedQuestion.setBody("Wat is de beste partij voor het milieu?");
        savedQuestion.setAuthor(testUser);
        savedQuestion.setQuestion(null);

        SecurityContextHolder.clearContext();
    }

    // Happy
    @Test
    @DisplayName("A top-level question can be created via POST endpoint")
    void testCreateTopLevelQuestion() throws Exception {
        String userEmail = "gert@hotmail.com";

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion question = invocation.getArgument(0);
            question.setId(1L);
            return question;
        });

        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("Wat is de beste partij voor het milieu?");

        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.body").value("Wat is de beste partij voor het milieu?"))
                .andExpect(jsonPath("$.data.author").exists())
                .andExpect(jsonPath("$.data.author.email").value("gert@hotmail.com"))
                .andExpect(jsonPath("$.data.author.name").value("Gert"));

        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
    }

    // Unhappy
    @Test
    @DisplayName("Creating a question without authentication returns 401 Unauthorized")
    void testCreateTopLevelQuestion_Unauthorized() throws Exception {
        SecurityContextHolder.clearContext();

        ForumQuestionDto requestDto = new ForumQuestionDto();
        requestDto.setBody("Wat is de beste partij voor het milieu?");

        mockMvc.perform(post("/api/forum/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());

        verify(userRepository, never()).findByEmail(anyString());
        verify(forumQuestionRepository, never()).save(any(ForumQuestion.class));
    }

    // Happy
    @Test
    @DisplayName("All top-level questions can be retrieved via GET endpoint")
    void testGetAllTopLevelQuestions() throws Exception {
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

        when(forumQuestionRepository.findAllTopLevelQuestions()).thenReturn(questions);

        mockMvc.perform(get("/api/forum/questions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].body").value("Vraag 1"))
                .andExpect(jsonPath("$[1].body").value("Vraag 2"));

        verify(forumQuestionRepository, times(1)).findAllTopLevelQuestions();
    }

    // Happy
    @Test
    @DisplayName("A specific question with comments can be retrieved via GET endpoint")
    void testGetQuestionById() throws Exception {
        Long questionId = 1L;

        ForumQuestion question = new ForumQuestion();
        question.setId(questionId);
        question.setBody("Hoofdvraag");
        question.setAuthor(testUser);
        question.setQuestion(null);

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

        when(forumQuestionRepository.findByIdWithAuthor(questionId)).thenReturn(Optional.of(question));
        when(forumQuestionRepository.findCommentsWithAuthor(questionId)).thenReturn(comments);

        mockMvc.perform(get("/api/forum/questions/{questionId}", questionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(questionId))
                .andExpect(jsonPath("$.body").value("Hoofdvraag"))
                .andExpect(jsonPath("$.author").exists())
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments.length()").value(2));

        verify(forumQuestionRepository, times(1)).findByIdWithAuthor(questionId);
        verify(forumQuestionRepository, times(1)).findCommentsWithAuthor(questionId);
    }

    // Unhappy
    @Test
    @DisplayName("Retrieving a non-existent question returns 404 Not Found")
    void testGetQuestionById_NotFound() throws Exception {
        Long questionId = 999L;
        when(forumQuestionRepository.findByIdWithAuthor(questionId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/forum/questions/{questionId}", questionId))
                .andExpect(status().isNotFound());

        verify(forumQuestionRepository, times(1)).findByIdWithAuthor(questionId);
        verify(forumQuestionRepository, never()).findCommentsWithAuthor(anyLong());
    }

    // Happy
    @Test
    @DisplayName("A comment can be created on a question via POST endpoint")
    void testCreateComment() throws Exception {
        String userEmail = "gert@hotmail.com";
        Long parentQuestionId = 1L;

        ForumQuestion parentQuestion = new ForumQuestion();
        parentQuestion.setId(parentQuestionId);
        parentQuestion.setBody("Bovenliggende vraag");
        parentQuestion.setAuthor(testUser);
        parentQuestion.setQuestion(null);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(userEmail);
        when(forumQuestionRepository.findById(parentQuestionId)).thenReturn(Optional.of(parentQuestion));
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(testUser));

        when(forumQuestionRepository.save(any(ForumQuestion.class))).thenAnswer(invocation -> {
            ForumQuestion comment = invocation.getArgument(0);
            comment.setId(2L);
            return comment;
        });

        ForumQuestionDto commentDto = new ForumQuestionDto();
        commentDto.setBody("Dit is een reactie");

        mockMvc.perform(post("/api/forum/{questionId}/questions", parentQuestionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.body").value("Dit is een reactie"))
                .andExpect(jsonPath("$.data.author.email").value("gert@hotmail.com"))
                .andExpect(jsonPath("$.data.author.name").value("Gert"));

        verify(forumQuestionRepository, times(1)).findById(parentQuestionId);
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(forumQuestionRepository, times(1)).save(any(ForumQuestion.class));
    }

    // Unhappy
    @Test
    @DisplayName("Creating a comment without authentication returns 401")
    void testCreateComment_Unauthorized() throws Exception {
        // Arrange: Create DTO for comment request
        ForumQuestionDto dto = new ForumQuestionDto();
        dto.setBody("Reactie");

        // Mock repository to return a parent question so controller gets past existence check
        ForumQuestion parentQuestion = new ForumQuestion();
        parentQuestion.setId(1L);
        when(forumQuestionRepository.findById(1L)).thenReturn(Optional.of(parentQuestion));

        // Clear SecurityContext to simulate unauthenticated user
        SecurityContextHolder.clearContext();

        // Act & Assert: Perform POST request and expect 401 Unauthorized
        mockMvc.perform(post("/api/forum/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());

        // Verify: UserRepository should not be called; repository should be checked for parent question
        verifyNoInteractions(userRepository);
        verify(forumQuestionRepository, times(1)).findById(1L);
    }

    // Unhappy
    @Test
    @DisplayName("Deleting a question by non-owner returns 403 Forbidden")
    void testDeleteQuestion_Forbidden() throws Exception {
        User otherUser = new User("Other", null, "other@mail.com", "pw");
        otherUser.setId(2L);

        ForumQuestion question = new ForumQuestion();
        question.setId(1L);
        question.setAuthor(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("other@mail.com");

        when(userRepository.findByEmail("other@mail.com")).thenReturn(Optional.of(otherUser));
        when(forumQuestionRepository.findById(1L)).thenReturn(Optional.of(question));

        mockMvc.perform(delete("/api/forum/questions/1"))
                .andExpect(status().isForbidden());
    }
}
