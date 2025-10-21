package antifraud.user;

import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {

    User findByUsername(String username);
    @Transactional
    void deleteByUsername(String username);

    Iterable<User> findAllByOrderByIdAsc();
}
