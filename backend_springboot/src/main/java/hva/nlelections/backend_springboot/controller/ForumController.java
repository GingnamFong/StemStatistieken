package hva.nlelections.backend_springboot.controller;

import hva.nlelections.backend_springboot.model.ForumPost;
import hva.nlelections.backend_springboot.repository.ForumPostRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    private final ForumPostRepository forumPostRepository;

    public ForumController(ForumPostRepository forumPostRepository) {
        this.forumPostRepository = forumPostRepository;
    }

    @PostMapping
    public ResponseEntity<ForumPost> createPost(@Valid @RequestBody ForumPost post) {
        ForumPost saved = forumPostRepository.save(post);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping
    public List<ForumPost> getAllPosts() {
        return forumPostRepository.findAll();
    }
}
