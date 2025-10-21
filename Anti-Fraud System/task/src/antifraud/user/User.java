package antifraud.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "user_id")
    private long id;

    @Column(name = "name")
    @Nonnull
    @NotBlank
    private String name;

    @Column(name = "username", unique = true)
    @Nonnull
    @NotBlank
    private String username;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Nullable
    //@NotBlank // checking manually
    private String password;

    @Column(name = "password_hash")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Nullable
    private String passwordHash;

    @Column(name = "role")
    private UserRole role;

    @Column(name = "is_locked")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private boolean isLocked;

    //constructor for JPARepository
    public User() {
    }

    public User(@Nonnull String name, @Nonnull String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }
}
