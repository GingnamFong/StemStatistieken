package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.CommentLike;
import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.CommentLikeRepository;
import nl.hva.ict.sm3.backend.repository.ForumQuestionRepository;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * REST controller responsible for managing likes on forum comments.
 *
 * <p>This controller exposes endpoints to like, unlike, and retrieve
 * the number of likes for a specific forum comment.
 *
 * <p>Only authenticated users are allowed to like or unlike comments.
 * A forum post can only be liked if it is a comment (i.e. it has a parent question).
 *
 * <p>This controller delegates persistence operations to repository classes.
 * Business rules such as authorization and comment validation are handled here.
 */
@RestController
@RequestMapping("/api/forum")
public class CommentLikeController {

    /**
     * Constructs a new {@code CommentLikeController}.
     *
     * @param commentLikeRepository repository for {@link CommentLike} entities
     * @param forumQuestionRepository repository for {@link ForumQuestion} entities
     * @param userRepository repository for {@link User} entities
     */

    private final CommentLikeRepository commentLikeRepository;
    private final ForumQuestionRepository forumQuestionRepository;
    private final UserRepository userRepository;

    /**
     * Likes a forum comment on behalf of the authenticated user.
     *
     * <p>If the user has already liked the comment, no new like is created.
     * Only comments (not top-level questions) can be liked.
     *
     * @param commentId the ID of the comment to like
     * @return HTTP 201 with the updated like count, or an error response
     */

    public CommentLikeController(CommentLikeRepository commentLikeRepository,
                                 ForumQuestionRepository forumQuestionRepository,
                                 UserRepository userRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.forumQuestionRepository = forumQuestionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> like(@PathVariable Long commentId) {
        User user = requireUser();

        ForumQuestion comment = forumQuestionRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.notFound().build();
        if (comment.getQuestion() == null) return ResponseEntity.status(400).body("Not a comment");

        if (!commentLikeRepository.existsByUserIdAndForumQuestionId(user.getId(), commentId)) {
            CommentLike like = new CommentLike();
            like.setUser(user);
            like.setForumQuestion(comment);

            commentLikeRepository.save(like);
        }

        long count = commentLikeRepository.countByForumQuestionId(commentId);
        return ResponseEntity.status(201).body(Map.of("commentId", commentId, "likes", count));
    }

    /**
     * Removes a like from a forum comment for the authenticated user.
     *
     * <p>If the user has not previously liked the comment, the operation
     * completes without error.
     *
     * @param commentId the ID of the comment to unlike
     * @return HTTP 200 with the updated like count
     */

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<?> unlike(@PathVariable Long commentId) {
        User user = requireUser();

        if (!forumQuestionRepository.existsById(commentId)) return ResponseEntity.notFound().build();

        commentLikeRepository.deleteByUserIdAndForumQuestionId(user.getId(), commentId);

        long count = commentLikeRepository.countByForumQuestionId(commentId);
        return ResponseEntity.ok(Map.of("commentId", commentId, "likes", count));
    }

    /**
     * Retrieves the number of likes for a forum comment.
     *
     * <p>If the user is authenticated, this endpoint also indicates whether
     * the current user has liked the comment.
     *
     * @param commentId the ID of the comment
     * @return HTTP 200 containing the like count and user-specific like status
     */

    @GetMapping("/comments/{commentId}/likes")
    public ResponseEntity<?> count(@PathVariable Long commentId) {
        if (!forumQuestionRepository.existsById(commentId)) return ResponseEntity.notFound().build();

        long count = commentLikeRepository.countByForumQuestionId(commentId);

        boolean likedByMe = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                likedByMe = commentLikeRepository.existsByUserIdAndForumQuestionId(user.getId(), commentId);
            }
        }

        return ResponseEntity.ok(Map.of("commentId", commentId, "likes", count, "likedByMe", likedByMe));
    }


    /**
     * Retrieves the currently authenticated user.
     *
     * @return the authenticated {@link User}
     * @throws ResponseStatusException if no authenticated user is present
     */
    private User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return userRepository.findByEmail(auth.getName()).orElseThrow(() ->
                new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED)
        );
    }
}

