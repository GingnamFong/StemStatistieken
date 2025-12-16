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
 * <p>Relationships:
 * <ul>
 *   <li>One-to-Many with {@link ForumPost} - A user can create multiple forum posts</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "users")
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
     * One-to-many relationship with ForumPost.
     * A user can create multiple forum posts.
     * 
     * <p>Cascade operations are set to ALL, meaning that when a user is deleted,
     * all associated forum posts will also be deleted. Fetch type is LAZY to
     * improve performance by loading posts only when needed. Orphan removal is
     * enabled to automatically remove posts when they are no longer associated
     * with a user.</p>
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ForumPost> forumPosts = new ArrayList<>();

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

    public String getFavoriteParty() { return favoriteParty; }
    public void setFavoriteParty(String favoriteParty) { this.favoriteParty = favoriteParty; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    /**
     * Gets the list of forum posts created by this user.
     *
     * @return a list of ForumPost entities associated with this user
     */
    public List<ForumPost> getForumPosts() {
        return forumPosts;
    }

    /**
     * Sets the list of forum posts for this user.
     * 
     * <p>Note: This method is typically managed by JPA. To add a post,
     * use {@link #addForumPost(ForumPost)} instead.</p>
     *
     * @param forumPosts the list of forum posts to associate with this user
     */
    public void setForumPosts(List<ForumPost> forumPosts) {
        this.forumPosts = forumPosts;
    }

    /**
     * Adds a forum post to this user's collection.
     * 
     * <p>This is a convenience method for adding forum posts to the user's collection.
     * Note: If ForumPost has a bidirectional relationship, ensure the user is also
     * set on the ForumPost entity separately.</p>
     *
     * @param forumPost the forum post to add
     */
    public void addForumPost(ForumPost forumPost) {
        forumPosts.add(forumPost);
    }

    /**
     * Removes a forum post from this user's collection.
     * 
     * <p>This method removes a forum post from the user's collection.
     * Note: If ForumPost has a bidirectional relationship, ensure the user reference
     * is also cleared on the ForumPost entity separately.</p>
     *
     * @param forumPost the forum post to remove
     */
    public void removeForumPost(ForumPost forumPost) {
        forumPosts.remove(forumPost);
    }
}

