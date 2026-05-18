package nicolagraziani.billbuddy.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponseDTO getOwnProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
        return new UserResponseDTO(currentAuthenticatedUser.getUserId(), currentAuthenticatedUser.getName(), currentAuthenticatedUser.getSurname(), currentAuthenticatedUser.getUsername(), currentAuthenticatedUser.getEmail(), currentAuthenticatedUser.getDateOfBirth(), currentAuthenticatedUser.getAvatarURL(), currentAuthenticatedUser.getRole(), currentAuthenticatedUser.isActive());
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateOwnProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
        this.userService.deactivateUserById(currentAuthenticatedUser.getUserId());
    }

    @GetMapping("/username/{username}")
    public PublicUserResponseDTO findByUsername(@PathVariable String username) {
        User found = this.userService.findByUsernameAndIsActive(username);
        return new PublicUserResponseDTO(found.getUserId(), found.getUsername(), found.getAvatarURL());
    }
}
