package nl.hva.ict.sm3.backend.test;

import nl.hva.ict.sm3.backend.model.CommentLike;
import nl.hva.ict.sm3.backend.model.ForumComment;
import nl.hva.ict.sm3.backend.model.ForumPost;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.CommentLikeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentLikeRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentLikeRepository repository;

    // ------------------------------
    // HAPPY FLOWS
    // ------------------------------

    @Test
    void HF_existsByUserIdAndCommentId_returnsTrue_whenLikeExists() {
        User author = new User("Author", "A", "author@test.com", "password123");
        em.persist(author);

        ForumPost post = new ForumPost();
        post.setQuestion("Q");
        post.setAnswer("A");
        post.setAuthor(author);
        em.persist(post);

        ForumComment comment = new ForumComment();
        comment.setAuthor(author);
        comment.setPost(post);
        comment.setBody("Nice post");
        em.persist(comment);

        User liker = new User("Liker", "L", "liker@test.com", "password123");
        em.persist(liker);

        CommentLike like = new CommentLike();
        like.setUser(liker);
        like.setComment(comment);
        em.persist(like);

        em.flush();
        em.clear();

        boolean exists = repository.existsByUserIdAndCommentId(
                liker.getId(), comment.getId()
        );

        assertThat(exists).isTrue();
    }

    @Test
    void HF_countByCommentId_returnsCorrectCount() {
        User u1 = new User("U1", "U", "u1@test.com", "password123");
        User u2 = new User("U2", "U", "u2@test.com", "password123");
        em.persist(u1);
        em.persist(u2);

        ForumPost post = new ForumPost();
        post.setQuestion("Q");
        post.setAnswer("A");
        post.setAuthor(u1);
        em.persist(post);

        ForumComment comment = new ForumComment();
        comment.setAuthor(u1);
        comment.setPost(post);
        comment.setBody("Popular comment");
        em.persist(comment);

        CommentLike l1 = new CommentLike();
        l1.setUser(u1);
        l1.setComment(comment);
        em.persist(l1);

        CommentLike l2 = new CommentLike();
        l2.setUser(u2);
        l2.setComment(comment);
        em.persist(l2);

        em.flush();
        em.clear();

        long count = repository.countByCommentId(comment.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    void HF_deleteByUserIdAndCommentId_removesLike() {
        User user = new User("Del", "User", "del@test.com", "password123");
        em.persist(user);

        ForumPost post = new ForumPost();
        post.setQuestion("Q");
        post.setAnswer("A");
        post.setAuthor(user);
        em.persist(post);

        ForumComment comment = new ForumComment();
        comment.setAuthor(user);
        comment.setPost(post);
        comment.setBody("Delete me");
        em.persist(comment);

        CommentLike like = new CommentLike();
        like.setUser(user);
        like.setComment(comment);
        em.persist(like);

        em.flush();
        em.clear();

        repository.deleteByUserIdAndCommentId(user.getId(), comment.getId());
        em.flush();

        assertThat(
                repository.existsByUserIdAndCommentId(user.getId(), comment.getId())
        ).isFalse();
    }

    // ------------------------------
    // UNHAPPY / ERROR HANDLING
    // ------------------------------

    @Test
    void EH_existsByUserIdAndCommentId_returnsFalse_whenNoLike() {
        boolean exists =
                repository.existsByUserIdAndCommentId(123L, 456L);

        assertThat(exists).isFalse();
    }

    @Test
    void EH_deleteByUserIdAndCommentId_doesNothing_whenLikeDoesNotExist() {
        repository.deleteByUserIdAndCommentId(999L, 888L);

        assertThat(
                repository.existsByUserIdAndCommentId(999L, 888L)
        ).isFalse();
    }
}