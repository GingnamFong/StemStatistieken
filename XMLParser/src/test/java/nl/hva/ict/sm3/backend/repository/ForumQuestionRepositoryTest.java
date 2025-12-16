
package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests voor ForumQuestionRepository.
 * 
 * Deze tests verifiëren dat de relaties tussen questions en comments correct werken:
 * - Een question (top-level) kan meerdere comments hebben
 * - Een comment heeft altijd één parent question
 * - Comments kunnen niet meerdere questions hebben
 */
@DataJpaTest
@DisplayName("ForumQuestion Repository Tests")
class ForumQuestionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ForumQuestionRepository forumQuestionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Maak een test gebruiker aan
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser = userRepository.save(testUser);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Een top-level question kan worden aangemaakt zonder parent")
    void testCreateTopLevelQuestion() {
        // Arrange & Act
        ForumQuestion question = new ForumQuestion();
        question.setBody("Dit is een top-level vraag");
        question.setQuestion(null); // Geen parent
        question.setAuthor(testUser);
        
        ForumQuestion saved = forumQuestionRepository.save(question);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuestion()).isNull();
        assertThat(saved.getBody()).isEqualTo("Dit is een top-level vraag");
        assertThat(saved.getAuthor()).isNotNull();
    }

    @Test
    @DisplayName("Een question kan meerdere comments hebben")
    void testQuestionCanHaveMultipleComments() {
        // Arrange: Maak een top-level question
        ForumQuestion parentQuestion = new ForumQuestion();
        parentQuestion.setBody("Hoofdvraag");
        parentQuestion.setQuestion(null);
        parentQuestion.setAuthor(testUser);
        parentQuestion = forumQuestionRepository.save(parentQuestion);
        entityManager.flush();
        entityManager.clear();

        // Act: Voeg meerdere comments toe
        ForumQuestion comment1 = new ForumQuestion();
        comment1.setBody("Eerste comment");
        comment1.setQuestion(parentQuestion);
        comment1.setAuthor(testUser);
        forumQuestionRepository.save(comment1);

        ForumQuestion comment2 = new ForumQuestion();
        comment2.setBody("Tweede comment");
        comment2.setQuestion(parentQuestion);
        comment2.setAuthor(testUser);
        forumQuestionRepository.save(comment2);

        ForumQuestion comment3 = new ForumQuestion();
        comment3.setBody("Derde comment");
        comment3.setQuestion(parentQuestion);
        comment3.setAuthor(testUser);
        forumQuestionRepository.save(comment3);

        entityManager.flush();
        entityManager.clear();

        // Assert: Check of alle comments zijn opgeslagen
        List<ForumQuestion> comments = forumQuestionRepository.findByQuestionIdOrderByCreatedAtAsc(parentQuestion.getId());
        assertThat(comments).hasSize(3);
        assertThat(comments.get(0).getBody()).isEqualTo("Eerste comment");
        assertThat(comments.get(1).getBody()).isEqualTo("Tweede comment");
        assertThat(comments.get(2).getBody()).isEqualTo("Derde comment");
        
        // Check dat alle comments dezelfde parent hebben
        assertThat(comments.get(0).getQuestion().getId()).isEqualTo(parentQuestion.getId());
        assertThat(comments.get(1).getQuestion().getId()).isEqualTo(parentQuestion.getId());
        assertThat(comments.get(2).getQuestion().getId()).isEqualTo(parentQuestion.getId());
    }

    @Test
    @DisplayName("Een comment heeft altijd één parent question")
    void testCommentHasOneParent() {
        // Arrange
        ForumQuestion parent = new ForumQuestion();
        parent.setBody("Parent vraag");
        parent.setQuestion(null);
        parent.setAuthor(testUser);
        parent = forumQuestionRepository.save(parent);
        entityManager.flush();
        entityManager.clear();

        // Act
        ForumQuestion comment = new ForumQuestion();
        comment.setBody("Dit is een comment");
        comment.setQuestion(parent);
        comment.setAuthor(testUser);
        ForumQuestion savedComment = forumQuestionRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        // Assert
        ForumQuestion retrieved = forumQuestionRepository.findById(savedComment.getId()).orElseThrow();
        assertThat(retrieved.getQuestion()).isNotNull();
        assertThat(retrieved.getQuestion().getId()).isEqualTo(parent.getId());
        assertThat(retrieved.getQuestion().getBody()).isEqualTo("Parent vraag");
    }

    @Test
    @DisplayName("findAllTopLevelQuestions retourneert alleen questions zonder parent")
    void testFindAllTopLevelQuestions() {
        // Arrange: Maak 2 top-level questions en 1 comment
        ForumQuestion question1 = new ForumQuestion();
        question1.setBody("Vraag 1");
        question1.setQuestion(null);
        question1.setAuthor(testUser);
        forumQuestionRepository.save(question1);

        ForumQuestion question2 = new ForumQuestion();
        question2.setBody("Vraag 2");
        question2.setQuestion(null);
        question2.setAuthor(testUser);
        ForumQuestion savedQuestion2 = forumQuestionRepository.save(question2);

        // Maak een comment (niet top-level)
        ForumQuestion comment = new ForumQuestion();
        comment.setBody("Comment op vraag 2");
        comment.setQuestion(savedQuestion2);
        comment.setAuthor(testUser);
        forumQuestionRepository.save(comment);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<ForumQuestion> topLevelQuestions = forumQuestionRepository.findAllTopLevelQuestions();

        // Assert: Alleen de 2 top-level questions, niet de comment
        assertThat(topLevelQuestions).hasSize(2);
        assertThat(topLevelQuestions).extracting(ForumQuestion::getBody)
                .containsExactlyInAnyOrder("Vraag 1", "Vraag 2");
        assertThat(topLevelQuestions).allMatch(q -> q.getQuestion() == null);
    }

    @Test
    @DisplayName("Wanneer een parent question wordt verwijderd, worden alle comments ook verwijderd (cascade)")
    void testCascadeDelete() {
        // Arrange
        ForumQuestion parent = new ForumQuestion();
        parent.setBody("Parent");
        parent.setQuestion(null);
        parent.setAuthor(testUser);
        parent = forumQuestionRepository.save(parent);

        ForumQuestion comment1 = new ForumQuestion();
        comment1.setBody("Comment 1");
        comment1.setQuestion(parent);
        comment1.setAuthor(testUser);
        ForumQuestion savedComment1 = forumQuestionRepository.save(comment1);

        ForumQuestion comment2 = new ForumQuestion();
        comment2.setBody("Comment 2");
        comment2.setQuestion(parent);
        comment2.setAuthor(testUser);
        ForumQuestion savedComment2 = forumQuestionRepository.save(comment2);

        entityManager.flush();
        entityManager.clear();

        // Verify comments bestaan
        assertThat(forumQuestionRepository.findById(savedComment1.getId())).isPresent();
        assertThat(forumQuestionRepository.findById(savedComment2.getId())).isPresent();

        // Act: Verwijder parent
        forumQuestionRepository.delete(parent);
        entityManager.flush();
        entityManager.clear();

        // Assert: Comments moeten ook verwijderd zijn (cascade)
        assertThat(forumQuestionRepository.findById(savedComment1.getId())).isEmpty();
        assertThat(forumQuestionRepository.findById(savedComment2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Een comment kan niet meerdere questions hebben (one-to-many relatie)")
    void testCommentCannotHaveMultipleParents() {
        // Arrange: Maak 2 parent questions
        ForumQuestion parent1 = new ForumQuestion();
        parent1.setBody("Parent 1");
        parent1.setQuestion(null);
        parent1.setAuthor(testUser);
        parent1 = forumQuestionRepository.save(parent1);

        ForumQuestion parent2 = new ForumQuestion();
        parent2.setBody("Parent 2");
        parent2.setQuestion(null);
        parent2.setAuthor(testUser);
        parent2 = forumQuestionRepository.save(parent2);

        // Act: Maak een comment en probeer beide parents te koppelen
        ForumQuestion comment = new ForumQuestion();
        comment.setBody("Comment");
        comment.setQuestion(parent1); // Eerst parent1
        comment.setAuthor(testUser);
        ForumQuestion saved = forumQuestionRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        // Verander naar parent2
        ForumQuestion retrieved = forumQuestionRepository.findById(saved.getId()).orElseThrow();
        retrieved.setQuestion(parent2);
        forumQuestionRepository.save(retrieved);
        entityManager.flush();
        entityManager.clear();

        // Assert: Comment heeft nu alleen parent2 (niet beide)
        ForumQuestion finalComment = forumQuestionRepository.findById(saved.getId()).orElseThrow();
        assertThat(finalComment.getQuestion().getId()).isEqualTo(parent2.getId());
        assertThat(finalComment.getQuestion().getId()).isNotEqualTo(parent1.getId());
        
        // Verify: parent1 heeft geen comments meer
        List<ForumQuestion> parent1Comments = forumQuestionRepository.findByQuestionIdOrderByCreatedAtAsc(parent1.getId());
        assertThat(parent1Comments).isEmpty();
    }
}

