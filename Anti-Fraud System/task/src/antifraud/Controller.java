package antifraud;

import antifraud.ex.WrongTransactionException;
import antifraud.exceptions.EmptyPasswordException;
import antifraud.exceptions.UserNotFoundException;
import antifraud.user.User;
import antifraud.user.UserRole;
import antifraud.user.UserService;
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
public class Controller {
    public static StatusWrapper allowed = new StatusWrapper(TransactionType.ALLOWED);
    public static StatusWrapper manualProcessing = new StatusWrapper(TransactionType.MANUAL_PROCESSING);
    public static StatusWrapper prohibited = new StatusWrapper(TransactionType.PROHIBITED);

    private final UserService userService;

    public Controller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = {"/api/antifraud/transaction", "/api/antifraud/transaction/"})
    public StatusWrapper checkTransaction(@RequestBody InputWrapper num) {
        if (num.amount > 0) {
            if (num.amount() <= 200) {
                return allowed;
            } else if (num.amount <= 1500) {
                return manualProcessing;
            } else {
                return prohibited;
            }
        } else {
            throw new WrongTransactionException();
        }
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
            throw new ResponseStatusException(NOT_FOUND);
        }
        return Map.of("username", username,
                "status", "Deleted successfully!");
    }

    @PutMapping("/api/auth/role")
    public User updateUser(@RequestBody UserWithRole user) {
        if (user.role != UserRole.MERCHANT && user.role != UserRole.SUPPORT) {
            throw new ResponseStatusException(BAD_REQUEST);
        }
        var userOld = userService.getUser(user.username());
        if (userOld == null) {
            throw new ResponseStatusException(NOT_FOUND);
        }
        if (userOld.getRole() == user.role()) {
            throw new ResponseStatusException(CONFLICT);
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
            throw new ResponseStatusException(BAD_REQUEST);
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

    public record InputWrapper(long amount) { }

    public record StatusWrapper(TransactionType result) { }

    public enum LockOperation {
        LOCK,
        UNLOCK
    }
}
