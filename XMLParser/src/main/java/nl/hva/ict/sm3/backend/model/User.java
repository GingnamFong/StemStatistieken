package nl.hva.ict.sm3.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the system.
 * 
 * <p>This entity stores user account information including authentication
 * credentials, personal details, and preferences. Users can create forum posts,
 * which are linked through a one-to-many relationship.</p>
 * 
 * <p>Performance optimizations:
 * <ul>
 *   <li>Lazy loading - Forum posts are loaded only when accessed</li>
 *   <li>Caching - Entity is cached at the second level to reduce database queries</li>
 * </ul>
 * </p>
 * 
 * <p>Relationships:
 * <ul>
 *   <li>One-to-Many with {@link ForumPost} - A user can create multiple forum Questions</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "users")
@Cacheable
@org.hibernate.annotations.Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 100)
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 100)
    @Column(name = "last_name")
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Column(name = "password", nullable = false)
    private String password; // Will be hashed

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Column(name = "favorite_party")
    private String favoriteParty;

    @Column(name = "profile_picture", columnDefinition = "TEXT")
    private String profilePicture; // encoded image or URL

    /**
     * One-to-many relationship with ForumQuestion.
     * A user can create multiple forum posts.
     *
     * <p>Performance optimizations:
     * <ul>
     *   <li>Cascade operations are set to ALL, meaning that when a user is deleted,
     *       all associated forum posts will also be deleted</li>
     *   <li>Fetch type is LAZY to improve performance by loading posts only when needed</li>
     *   <li>Orphan removal is enabled to automatically remove posts when they are no longer
     *       associated with a user</li>
     * </ul>
     * </p>
     *
     * <p>Lazy loading strategy: The forum posts collection is not loaded from the database
     * until it is explicitly accessed, reducing initial query overhead and memory usage.
     * This prevents N+1 query problems and improves application performance.</p>
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ForumQuestion> forumQuestions = new ArrayList<>();

    /**
     * One-to-many relationship with CommentLike.
     * A user can like multiple comments.
     *
     * <p>Performance optimizations:
     * <ul>
     *   <li>Fetch type is LAZY - likes are only loaded when explicitly accessed</li>
     *   <li>No cascade delete - likes are deleted when user is deleted </li>
     * </ul>
     * </p>
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<CommentLike> commentLikes = new ArrayList<>();

    /**
     * Many-to-one relationship with Party (favorite party).
     * A user can have one favorite party.
     *
     * <p>Performance optimizations:
     * <ul>
     *   <li>Fetch type is EAGER - favorite party is usually needed when displaying profile</li>
     *   <li>No cascade - party should not be deleted when user is deleted</li>
     * </ul>
     * </p>
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "favorite_party_id")
    private Party favoritePartyEntity;

    /**
     * Default constructor for JPA.
     */
    public User() {
    }

    public User(String name, String lastName, String email, String password) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDate) {
        this.birthDate = birthDate;
    }

    public String getFavoriteParty() {
        return favoriteParty;
    }

    public void setFavoriteParty(String favoriteParty) {
        this.favoriteParty = favoriteParty;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * Gets the list of forum posts created by this user.
     *
     * @return a list of ForumPost entities associated with this user
     */
    public List<ForumQuestion> getForumQuestions() {
        return forumQuestions;
    }

    public void setForumQuestions(List<ForumQuestion> forumQuestions) {
        this.forumQuestions = forumQuestions;
    }

    public void addForumQuestion(ForumQuestion forumQuestion) {
        forumQuestions.add(forumQuestion);
        forumQuestion.setAuthor(this); // belangrijk: beide kanten sync
    }

    public void removeForumQuestion(ForumQuestion forumQuestion) {
        forumQuestions.remove(forumQuestion);
        forumQuestion.setAuthor(null);
    }

    /**
     * Gets the list of comment likes given by this user.
     *
     * @return a list of CommentLike entities associated with this user
     */
    public List<CommentLike> getCommentLikes() {
        return commentLikes;
    }

    public void setCommentLikes(List<CommentLike> commentLikes) {
        this.commentLikes = commentLikes;
    }

    public void addCommentLike(CommentLike commentLike) {
        commentLikes.add(commentLike);
        commentLike.setUser(this);
    }

    public void removeCommentLike(CommentLike commentLike) {
        commentLikes.remove(commentLike);
        commentLike.setUser(null);
    }

    /**
     * Gets the favorite party entity (if using relationship instead of string).
     *
     * @return the favorite Party entity
     */
    public Party getFavoritePartyEntity() {
        return favoritePartyEntity;
    }

    public void setFavoritePartyEntity(Party favoritePartyEntity) {
        this.favoritePartyEntity = favoritePartyEntity;
    }
}

