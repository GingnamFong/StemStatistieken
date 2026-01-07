package nl.hva.ict.sm3.backend.repository;

import nl.hva.ict.sm3.backend.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository for User profile operations using EntityManager.
 * Demonstrates lazy/eager fetching with fetch joins and cascading operations.
 */
@Repository
public class ProfileRepository {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Find user by ID with all relationships eagerly loaded.
     * Uses fetch joins to prevent N+1 queries.
     * LAZY relationships (forumQuestions, commentLikes) are loaded eagerly here.
     */
    @Transactional(readOnly = true)
    public Optional<User> findByIdWithAllRelations(Long id) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.forumQuestions " +
            "LEFT JOIN FETCH u.commentLikes " +
            "LEFT JOIN FETCH u.favoritePartyEntity " +
            "WHERE u.id = :id",
            User.class
        );
        query.setParameter("id", id);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Find user by email with all relationships.
     * EAGER fetch for favoritePartyEntity, LAZY for collections (loaded via fetch join).
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmailWithAllRelations(String email) {
        TypedQuery<User> query = entityManager.createQuery(
            "SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.forumQuestions " +
            "LEFT JOIN FETCH u.commentLikes " +
            "LEFT JOIN FETCH u.favoritePartyEntity " +
            "WHERE u.email = :email",
            User.class
        );
        query.setParameter("email", email);
        try {
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Save user with cascading to related entities.
     * CascadeType.ALL on forumQuestions, CascadeType.REMOVE on commentLikes.
     */
    @Transactional
    public User save(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            user = entityManager.merge(user);
        }
        return user;
    }
}

