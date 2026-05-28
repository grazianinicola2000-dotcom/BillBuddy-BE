package nicolagraziani.billbuddy.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndIsActiveTrue(String email);

    Optional<User> findByUsernameAndIsActiveTrue(String username);

    Optional<User> findByUserIdAndIsActiveTrue(UUID activeUserId);

    Page<User> findByUsernameContainingIgnoreCaseAndIsActiveTrue(String username, Pageable pageable);

    List<User> findTop10ByUsernameContainingIgnoreCaseAndIsActiveTrue(String username);
}
