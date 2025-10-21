package antifraud.user;

import antifraud.exceptions.DuplicateUserException;
import antifraud.exceptions.UserNotFoundException;
import io.micrometer.common.lang.NonNullApi;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@NonNullApi
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    public List<User> getAll() {
        return StreamSupport.stream(userRepository.findAllByOrderByIdAsc().spliterator(), false).toList();
    }

    public User saveUser(User user) {
        var r = loadUserByUsername(user.getUsername());
        if (r != null) {
            throw new DuplicateUserException();
        }
        user.setPasswordHash(encoder.encode(user.getPassword()));
        user.setUsername(user.getUsername().toLowerCase());
        if (userRepository.count() == 0) {
            user.setRole(UserRole.ADMINISTRATOR);
            user.setLocked(false);
        } else {
            user.setRole(UserRole.MERCHANT);
            user.setLocked(true);
        }
        var newUser = userRepository.save(user);
        user.setId(newUser.getId());
        return user;
    }

    @Override
    @Nullable
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var x = userRepository.findByUsername(username);
        if (x != null) {
            return new org.springframework.security.core.userdetails.User(
                    x.getUsername(),
                    x.getPasswordHash(),
                    true,
                    true,
                    true,
                    !x.isLocked(),
                    List.of(new SimpleGrantedAuthority(x.getRole().toString())));
        } else {
            return null;
        }
    }

    @Nullable
    public User getUser(String username) {
        return userRepository.findByUsername(username);
    }


    public boolean deleteUser(String username) {
        if (loadUserByUsername(username) != null) {
            userRepository.deleteByUsername(username);
            return true;
        } else {
            return false;
        }
    }

    public void updateRoleForUser(User user, UserRole role) {
        user.setRole(role);
        userRepository.save(user);
    }

    public void lockUser(@NotBlank String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException();
        }
        user.setLocked(true);
        userRepository.save(user);
    }

    public void unlockUser(@NotBlank String username) {
        var user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException();
        }
        user.setLocked(false);
        userRepository.save(user);
    }
}
