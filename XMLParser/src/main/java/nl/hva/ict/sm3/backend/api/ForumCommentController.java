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


/**
 * REST controller for managing forum comments.
 *
 * <p>This controller supports:
 * <ul>
 *   <li>Retrieving comments for a specific forum post</li>
 *   <li>Adding a new comment to a post (authenticated users only)</li>
 *   <li>Deleting a comment (only the owner/author of the post can delete)</li>
 * </ul>
 *
 * <p>Authentication is derived from the {@link SecurityContextHolder}. The authenticated
 * principal is expected to be the user's email address.
 */
@RestController
@RequestMapping("/api/forum")
public class ForumCommentController {

    private final ForumPostRepository forumPostRepository;
    private final ForumCommentRepository forumCommentRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new {@code ForumCommentController}.
     *
     * @param forumPostRepository    repository for accessing forum posts
     * @param forumCommentRepository repository for accessing forum comments
     * @param userRepository         repository for accessing users
     */
    public ForumCommentController(ForumPostRepository forumPostRepository,
                                  ForumCommentRepository forumCommentRepository,
                                  UserRepository userRepository) {
        this.forumPostRepository = forumPostRepository;
        this.forumCommentRepository = forumCommentRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all comments for a given forum post, ordered by creation time ascending.
     *
     * <p>HTTP status codes:
     * <ul>
     *   <li>200 OK - Comments retrieved successfully</li>
     *   <li>404 Not Found - The post does not exist</li>
     * </ul>
     *
     * @param postId the ID of the forum post
     * @return a list of {@link ForumComment} objects
     */
    // GET /api/forum/{postId}/comments
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<ForumComment>> getComments(@PathVariable Long postId) {
        if (!forumPostRepository.existsById(postId)) {
            return ResponseEntity.notFound().build();
        }
        List<ForumComment> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * Adds a new comment to a given forum post.
     *
     * <p>The request requires authentication. The logged-in user's email is obtained
     * from {@link Authentication#getName()} and used to load the {@link User}.
     *
     * <p>HTTP status codes:
     * <ul>
     *   <li>201 Created - Comment created successfully</li>
     *   <li>401 Unauthorized - User is not authenticated or user not found</li>
     *   <li>404 Not Found - The post does not exist</li>
     * </ul>
     *
     * @param postId the ID of the forum post
     * @param dto    the request body containing the comment text (validated)
     * @return the created {@link ForumComment}
     */
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

    /**
     * Deletes a comment by its ID.
     *
     * <p>Authorization rule:
     * <ul>
     *   <li>Only the owner/author of the related forum post is allowed to delete comments.</li>
     * </ul>
     *
     * <p>HTTP status codes:
     * <ul>
     *   <li>204 No Content - Comment deleted successfully</li>
     *   <li>401 Unauthorized - User is not authenticated or user not found</li>
     *   <li>403 Forbidden - Authenticated user is not the owner of the post</li>
     *   <li>404 Not Found - Comment does not exist</li>
     * </ul>
     *
     * @param commentId the ID of the comment to delete
     * @return empty response indicating the outcome
     */
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
