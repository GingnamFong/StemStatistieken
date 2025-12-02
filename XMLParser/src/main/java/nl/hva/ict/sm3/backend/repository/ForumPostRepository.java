package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {
}

