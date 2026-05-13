package nicolagraziani.billbuddy.user;


import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder bcrypt;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder bcrypt, UserRepository userRepository) {
        this.bcrypt = bcrypt;
        this.userRepository = userRepository;
    }

    public UserResponseDTO saveUser(UserDTO body) {

        if (this.userRepository.existsByEmail(body.email())) {
            throw new BadRequestException("Email already taken!");
        }
        if (this.userRepository.existsByUsername(body.username())) {
            throw new BadRequestException("Username already taken!");
        }

        User newUser = new User(body.name(), body.surname(), body.username(), body.email().toLowerCase(), this.bcrypt.encode(body.password()), body.dateOfBirth());
        this.userRepository.save(newUser);
        log.info("User {} {} has been successfully registered", body.name(), body.surname());
        return new UserResponseDTO(newUser.getUserId(), newUser.getName(), newUser.getSurname(), newUser.getUsername(), newUser.getEmail(), newUser.getDateOfBirth(), newUser.getAvatarURL(), newUser.getRole(), newUser.isActive());
    }

    public Page<UserResponseDTO> findAll(int page, int size, String sortBy) {
        if (size > 100 || size < 0) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.userRepository.findAll(pageable)
                .map(user -> new UserResponseDTO(
                        user.getUserId(), user.getName(), user.getSurname(), user.getUsername(), user.getEmail(), user.getDateOfBirth(), user.getAvatarURL(), user.getRole(), user.isActive()
                ));
    }

    public User findUserById(UUID userId) {
        return this.userRepository.findById(userId).orElseThrow(() -> new NotFoundException(userId));
    }

    public User findActiveUserById(UUID activeUserId) {
        return this.userRepository.findByUserIdAndIsActiveTrue(activeUserId).orElseThrow(() -> new NotFoundException(activeUserId));
    }

    public User findByEmailAndIsActive(String email) {
        return this.userRepository.findByEmailAndIsActiveTrue(email).orElseThrow(() -> new NotFoundException("User with email " + email + " not found"));
    }

    public User findByUsernameAndIsActive(String username) {
        return this.userRepository.findByUsernameAndIsActiveTrue(username).orElseThrow(() -> new NotFoundException("User with username " + username + " not found"));
    }

    public void deactivateUserById(UUID userId) {
        User found = this.findUserById(userId);
        if (!found.isActive()) {
            throw new BadRequestException("User is already inactive");
        }
        found.setActive(false);
        this.userRepository.save(found);
        log.info("User {} has been successfully deactivated", found.getUsername());
    }

    public void activateUserById(UUID userId) {
        User found = this.findUserById(userId);
        if (found.isActive()) {
            throw new BadRequestException("User is already active");
        }
        found.setActive(true);
        this.userRepository.save(found);
        log.info("User {} has been successfully activated", found.getUsername());
    }
}
