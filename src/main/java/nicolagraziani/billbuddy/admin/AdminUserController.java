package nicolagraziani.billbuddy.admin;

import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserResponseDTO;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Page<UserResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "surname") String sortBy) {
        return this.userService.findAll(page, size, sortBy);
    }

    @GetMapping("/{userId}")
    public UserResponseDTO findById(@PathVariable UUID userId) {

        User found = this.userService.findUserById(userId);
        return new UserResponseDTO(found.getUserId(), found.getName(), found.getSurname(), found.getUsername(), found.getEmail(), found.getDateOfBirth(), found.getAvatarURL(), found.getRole(), found.isActive());
    }

    @PatchMapping("/{userId}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(@PathVariable UUID userId) {
        this.userService.deactivateUserById(userId);
    }

    @PatchMapping("/{userId}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activateUser(@PathVariable UUID userId) {
        this.userService.activateUserById(userId);
    }
}
