package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.model.CommentLike;
import nl.hva.ict.sm3.backend.model.ForumQuestion;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.CommentLikeRepository;
import nl.hva.ict.sm3.backend.repository.ForumQuestionRepository;
import nl.hva.ict.sm3.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class CommentLikeController {

    private final CommentLikeRepository commentLikeRepository;
    private final ForumQuestionRepository forumQuestionRepository;
    private final UserRepository userRepository;

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

    @DeleteMapping("/comments/{commentId}/like")
    public ResponseEntity<?> unlike(@PathVariable Long commentId) {
        User user = requireUser();

        if (!forumQuestionRepository.existsById(commentId)) return ResponseEntity.notFound().build();

        commentLikeRepository.deleteByUserIdAndForumQuestionId(user.getId(), commentId);

        long count = commentLikeRepository.countByForumQuestionId(commentId);
        return ResponseEntity.ok(Map.of("commentId", commentId, "likes", count));
    }

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

