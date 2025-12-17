
package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.CommentLike;
import nl.hva.ict.sm3.backend.model.ForumComment;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.CommentLikeRepository;
import nl.hva.ict.sm3.backend.repository.ForumCommentRepository;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class CommentLikeController {

    private final CommentLikeRepository commentLikeRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;

    public CommentLikeController(CommentLikeRepository commentLikeRepository,
                                 ForumCommentRepository forumCommentRepository,
                                 UserRepository userRepository) {
        this.commentLikeRepository = commentLikeRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.userRepository = userRepository;
    }

    // POST /api/forum/comments/{commentId}/like
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> like(@PathVariable Long commentId) {
        User user = requireUser();

        ForumComment comment = forumCommentRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.notFound().build();

        if (commentLikeRepository.existsByUserIdAndCommentId(user.getId(), commentId)) {
            return ResponseEntity.status(409).body("Already liked");
        }

        CommentLike like = new CommentLike();
        like.setUser(user);
        like.setComment(comment);
        commentLikeRepository.save(like);

        long count = commentLikeRepository.countByCommentId(commentId);
        return ResponseEntity.status(201).body(Map.of(
                "commentId", commentId,
                "likes", count
        ));
    }

    // DELETE /api/forum/comments/{commentId}/like
    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<?> unlike(@PathVariable Long commentId) {
        User user = requireUser();

        if (!forumCommentRepository.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }

        commentLikeRepository.deleteByUserIdAndCommentId(user.getId(), commentId);

        long count = commentLikeRepository.countByCommentId(commentId);
        return ResponseEntity.ok(Map.of(
                "commentId", commentId,
                "likes", count
        ));
    }

    // GET /api/forum/comments/{commentId}/likes
    @GetMapping("/comments/{commentId}/likes")
    public ResponseEntity<?> count(@PathVariable Long commentId) {
        if (!forumCommentRepository.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }

        long count = commentLikeRepository.countByCommentId(commentId);

        boolean likedByMe = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = userRepository.findByEmail(auth.getName()).orElse(null);
            if (user != null) {
                likedByMe = commentLikeRepository
                        .existsByUserIdAndCommentId(user.getId(), commentId);
            }
        }

        return ResponseEntity.ok(Map.of(
                "commentId", commentId,
                "likes", count,
                "likedByMe", likedByMe
        ));
    }

    /**
     * Ensures the user is authenticated and returns the User entity.
     */
    private User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Not authenticated"
            );
        }

        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED,
                        "User not found"
                ));
    }
}



