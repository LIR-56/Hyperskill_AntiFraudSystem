package antifraud.user;

import antifraud.exceptions.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;


@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/user")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new EmptyPasswordException();
        }
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/api/auth/list")
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @DeleteMapping("/api/auth/user/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        if (!userService.deleteUser(username)) {
            throw new UserNotFoundException();
        }
        return Map.of("username", username,
                "status", "Deleted successfully!");
    }

    @PutMapping("/api/auth/role")
    public User updateUser(@RequestBody UserWithRole user) {
        if (user.role != UserRole.MERCHANT && user.role != UserRole.SUPPORT) {
            throw new WrongUserRoleException();
        }
        var userOld = userService.getUser(user.username());
        if (userOld == null) {
            throw new UserNotFoundException();
        }
        if (userOld.getRole() == user.role()) {
            throw new SameUserRoleException();
        }
        userService.updateRoleForUser(userOld, user.role());
        return userService.getUser(user.username);
    }

    @PutMapping("/api/auth/access")
    public Map<String, String> lockOperation(@RequestBody @Valid UserWithLocking user) {
        var oldUser = userService.getUser(user.username());
        if (oldUser == null) {
            throw new UserNotFoundException();
        }
        if (oldUser.getRole() == UserRole.ADMINISTRATOR) {
            throw new AttemptToChangeAdministratorRoleException();
        }
        String result;
        if (user.operation() == LockOperation.LOCK) {
            userService.lockUser(user.username());
            result = String.format("User %s %s!", user.username(), "locked");
        } else if (user.operation() == LockOperation.UNLOCK) {
            userService.unlockUser(user.username());
            result = String.format("User %s %s!", user.username(), "unlocked");
        } else {
            throw new ResponseStatusException(BAD_REQUEST);
        }
        return Map.of("status", result);
    }


    public record UserWithLocking(@NotBlank String username, LockOperation operation) { }
    public record UserWithRole(String username, UserRole role) { }

    public enum LockOperation {
        LOCK,
        UNLOCK
    }
}
