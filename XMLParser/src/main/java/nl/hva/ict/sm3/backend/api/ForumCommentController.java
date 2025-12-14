package nl.hva.ict.sm3.backend.api;

import jakarta.validation.Valid;
import nl.hva.ict.sm3.backend.dto.ForumCommentDto;
import nl.hva.ict.sm3.backend.model.ForumComment;
import nl.hva.ict.sm3.backend.model.ForumPost;
import nl.hva.ict.sm3.backend.repository.ForumCommentRepository;
import nl.hva.ict.sm3.backend.repository.ForumPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumCommentController {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;

    public ForumCommentController(ForumPostRepository forumPostRepository,
                                  ForumCommentRepository forumCommentRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
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

        ForumComment comment = new ForumComment();
        comment.setBody(dto.getBody());
        comment.setPost(post);

        ForumComment saved = forumCommentRepository.save(comment);
        return ResponseEntity.status(201).body(saved);
    }
}
