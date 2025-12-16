package nl.hva.ict.sm3.backend.api;

import nl.hva.ict.sm3.backend.repository.Forum2Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public class Forum2Controller {
    private final Forum2Repository repository;

    public Forum2Controller(Forum2Repository repository) {
        this.repository = repository;
    }
    /*
    // GET all posts
    @GetMapping
    public List<Post> getPosts() {
        return repository.findAll();
    }

    // CREATE post
    @PostMapping
    public Post createPost(@RequestBody Post post) {
        return repository.save(post);
    }

    // VOTE
    @PatchMapping("/{id}/vote")
    public Post vote(
            @PathVariable Long id,
            @RequestParam String type
    ) {
        Post post = repository.findById(id).orElseThrow();

        if (type.equals("up")) post.setScore(post.getScore() + 1);
        if (type.equals("down")) post.setScore(post.getScore() - 1);

        return repository.save(post);
    }

}

     */
