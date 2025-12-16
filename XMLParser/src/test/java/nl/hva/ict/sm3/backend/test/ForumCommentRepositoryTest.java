package nl.hva.ict.sm3.backend.test;

import nl.hva.ict.sm3.backend.model.ForumComment;
import nl.hva.ict.sm3.backend.model.ForumPost;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.ForumCommentRepository;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ForumCommentRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ForumCommentRepository repository;

    // ------------------------------
    // HAPPY FLOWS
    // ------------------------------

    @Test
    void HF_findByPostIdOrderByCreatedAtAsc_returnsCommentsInChronologicalOrder() {
        User author = new User("Alice", "A", "alice@test.com", "password123");
        em.persist(author);

        ForumPost post = new ForumPost();
        post.setQuestion("Question?");
        post.setAnswer("Answer");
        post.setAuthor(author);
        em.persist(post);

        ForumComment c1 = new ForumComment();
        c1.setAuthor(author);
        c1.setPost(post);
        c1.setBody("First comment");
        em.persist(c1);

        ForumComment c2 = new ForumComment();
        c2.setAuthor(author);
        c2.setPost(post);
        c2.setBody("Second comment");
        em.persist(c2);

        em.flush();
        em.clear();

        List<ForumComment> comments =
                repository.findByPostIdOrderByCreatedAtAsc(post.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getBody()).isEqualTo("First comment");
        assertThat(comments.get(1).getBody()).isEqualTo("Second comment");
    }

    @Test
    void HF_findByIdWithPostAuthor_fetchesPostAndAuthorEagerly() {
        User author = new User("Bob", "B", "bob@test.com", "password123");
        em.persist(author);

        ForumPost post = new ForumPost();
        post.setQuestion("Q?");
        post.setAnswer("A");
        post.setAuthor(author);
        em.persist(post);

        ForumComment comment = new ForumComment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setBody("Hello");
        em.persist(comment);

        em.flush();
        em.clear();

        Optional<ForumComment> result =
                repository.findByIdWithPostAuthor(comment.getId());

        assertThat(result).isPresent();

        ForumComment loaded = result.get();

        // Verify JOIN FETCH worked
        assertThat(Hibernate.isInitialized(loaded.getPost())).isTrue();
        assertThat(Hibernate.isInitialized(loaded.getPost().getAuthor())).isTrue();
        assertThat(loaded.getPost().getAuthor().getEmail())
                .isEqualTo("bob@test.com");
    }

    // ------------------------------
    // UNHAPPY / ERROR HANDLING
    // ------------------------------

    @Test
    void EH_findByPostIdOrderByCreatedAtAsc_returnsEmptyList_whenNoComments() {
        User author = new User("No", "Comments", "none@test.com", "password123");
        em.persist(author);

        ForumPost post = new ForumPost();
        post.setQuestion("Empty?");
        post.setAnswer("Yes");
        post.setAuthor(author);
        em.persist(post);

        em.flush();
        em.clear();

        List<ForumComment> comments =
                repository.findByPostIdOrderByCreatedAtAsc(post.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void EH_findByIdWithPostAuthor_returnsEmpty_whenCommentDoesNotExist() {
        Optional<ForumComment> result =
                repository.findByIdWithPostAuthor(9999L);

        assertThat(result).isEmpty();
    }
}
