package antifraud.user;

import antifraud.exceptions.DuplicateUserException;
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
        var newUser = userRepository.save(user);
        user.setId(newUser.getId());
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var x = userRepository.findByUsername(username);
        if (x != null) {
            return new org.springframework.security.core.userdetails.User(
                    x.getUsername(),
                    x.getPasswordHash(),
                    List.of(new SimpleGrantedAuthority("USER")));
        } else {
            return null;
        }
    }

    public boolean deleteUser(String username) {
        if (loadUserByUsername(username) != null) {
            userRepository.deleteByUsername(username);
            return true;
        } else {
            return false;
        }
    }
}
