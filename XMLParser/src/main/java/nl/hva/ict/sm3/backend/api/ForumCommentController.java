package nl.hva.ict.sm3.backend.api;

import jakarta.validation.Valid;
import nl.hva.ict.sm3.backend.dto.ForumCommentDto;
import nl.hva.ict.sm3.backend.model.ForumComment;
import nl.hva.ict.sm3.backend.model.ForumPost;
import nl.hva.ict.sm3.backend.repository.ForumCommentRepository;
import nl.hva.ict.sm3.backend.repository.ForumPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;
import nl.hva.ict.sm3.backend.model.User;
import nl.hva.ict.sm3.backend.repository.UserRepository;



import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumCommentController {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;

    public ForumCommentController(ForumPostRepository forumPostRepository,
                                  ForumCommentRepository forumCommentRepository,
                                  UserRepository userRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.userRepository = userRepository;
    }

    // GET /api/forum/{postId}/comments
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<ForumComment>> getComments(@PathVariable Long postId) {
        if (!forumPostRepository.existsById(postId)) {
            return ResponseEntity.notFound().build();
        }
        List<ForumComment> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return ResponseEntity.ok(comments);
    }

    // POST /api/forum/{postId}/comments
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ForumComment> addComment(@PathVariable Long postId,
                                                   @Valid @RequestBody ForumCommentDto dto) {
        ForumPost post = forumPostRepository.findById(postId).orElse(null);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).build();
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        ForumComment comment = new ForumComment();
        comment.setBody(dto.getBody());
        comment.setPost(post);
        comment.setAuthor(user);

        ForumComment saved = forumCommentRepository.save(comment);
        return ResponseEntity.status(201).body(saved);
    }
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {

        Optional<ForumComment> commentOpt = forumCommentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ForumComment comment = commentOpt.get();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        if (comment.getPost() == null
                || comment.getPost().getAuthor() == null
                || !comment.getPost().getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        forumCommentRepository.delete(comment);
        return ResponseEntity.noContent().build();
    }

}
