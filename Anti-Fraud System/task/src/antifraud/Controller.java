package antifraud;

import antifraud.user.User;
import antifraud.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;


@RestController
public class Controller {
    public static StatusWrapper allowed = new StatusWrapper(TransactionType.ALLOWED);
    public static StatusWrapper manualProcessing = new StatusWrapper(TransactionType.MANUAL_PROCESSING);
    public static StatusWrapper prohibited = new StatusWrapper(TransactionType.PROHIBITED);

    private final UserService userService;

    public Controller(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/antifraud/transaction")
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/api/auth/user")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        return new ResponseEntity<>(userService.saveUser(user), HttpStatus.CREATED);
    }

    @GetMapping("/api/auth/list")
    public List<User> getAllUsers() {
        return userService.getAll();
    }

    @DeleteMapping("/api/auth/user/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        if (!userService.deleteUser(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return Map.of("username", username,
                "status", "Deleted successfully!");
    }

    public record InputWrapper(long amount) {
    }

    public record StatusWrapper(TransactionType result) {
    }
}
