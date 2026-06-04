package nicolagraziani.billbuddy.user;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.group.repositories.GroupMemberRepository;
import nicolagraziani.billbuddy.group.repositories.GroupRepository;
import nicolagraziani.billbuddy.group.repositories.InviteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder bcrypt;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final InviteRepository inviteRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final Cloudinary cloudinary;

    public UserService(PasswordEncoder bcrypt, UserRepository userRepository, GroupRepository groupRepository, InviteRepository inviteRepository, GroupMemberRepository groupMemberRepository, Cloudinary cloudinary) {
        this.bcrypt = bcrypt;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.inviteRepository = inviteRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.cloudinary = cloudinary;
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
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

    public List<PublicUserResponseDTO> searchUsers(String partialUsername) {
        Pageable pageable = PageRequest.of(0, 10);
        if (partialUsername.length() < 2) return List.of();
        return this.userRepository.findByUsernameContainingIgnoreCaseAndIsActiveTrue(partialUsername, pageable)
                .stream().map(user -> new PublicUserResponseDTO(
                        user.getUserId(),
                        user.getUsername(),
                        user.getAvatarURL()
                )).toList();
    }

    public List<PublicUserResponseDTO> findInviteableUsers(UUID groupId, String query, User currentUser) {
        Group foundGroup = this.groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException(groupId));
        List<User> users = this.userRepository.findTop10ByUsernameContainingIgnoreCaseAndIsActiveTrue(query);

        return users.stream().filter(user -> !user.getUserId().equals(currentUser.getUserId()))
                .filter(user -> !this.groupMemberRepository.existsByGroupAndUser(foundGroup, user))
                .filter(user -> this.inviteRepository.findByGroupAndReceiverAndStatus(foundGroup, user, InviteStatus.PENDING).isEmpty())
                .map(user -> new PublicUserResponseDTO(user.getUserId(), user.getUsername(), user.getAvatarURL())).toList();
    }

    public UserResponseDTO userImgUpload(MultipartFile file, UUID employeeId) {
        User found = this.findUserById(employeeId);
        if (file.isEmpty()) {
            throw new BadRequestException("No file uploaded");
        }
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }
        if (file.getSize() > 5_000_000) {
            throw new BadRequestException("Image size must be less than 5 MB");
        }
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String url = (String) result.get("secure_url");
            found.setAvatarURL(url);
            this.userRepository.save(found);
            log.info("User {} updated profile image", found.getUsername());
            return new UserResponseDTO(
                    found.getUserId(),
                    found.getName(),
                    found.getSurname(),
                    found.getUsername(),
                    found.getEmail(),
                    found.getDateOfBirth(),
                    found.getAvatarURL(),
                    found.getRole(),
                    found.isActive()
            );
        } catch (IOException e) {
            log.error("Failed to upload profile image for user {}", found.getUsername(), e);
            throw new BadRequestException("Failed to upload image. Please try again.");
        }
    }
}
