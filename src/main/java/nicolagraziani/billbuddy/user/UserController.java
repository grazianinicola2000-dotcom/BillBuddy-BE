package nicolagraziani.billbuddy.user;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public Page<UserResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(defaultValue = "surname") String sortBy) {
        return this.userService.findAll(page, size, sortBy);
    }

    @GetMapping("/me")
    public UserResponseDTO getOwnProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
        return new UserResponseDTO(currentAuthenticatedUser.getUserId(), currentAuthenticatedUser.getName(), currentAuthenticatedUser.getSurname(), currentAuthenticatedUser.getUsername(), currentAuthenticatedUser.getEmail(), currentAuthenticatedUser.getDateOfBirth(), currentAuthenticatedUser.getAvatarURL(), currentAuthenticatedUser.getRole());
    }

    //    TODO: implementare soft delete dell'utente
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwnProfile(@AuthenticationPrincipal User currentAuthenticatedUser) {
        this.userService.findUserByIdAndDelete(currentAuthenticatedUser.getUserId());
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public UserResponseDTO findById(@PathVariable UUID userId) {

        User found = this.userService.findUserById(userId);
        return new UserResponseDTO(found.getUserId(), found.getName(), found.getSurname(), found.getUsername(), found.getEmail(), found.getDateOfBirth(), found.getAvatarURL(), found.getRole());
    }

    @GetMapping("/username/{username}")
    public PublicUserResponseDTO findByUsername(@PathVariable String username) {
        User found = this.userService.findByUsername(username);
        return new PublicUserResponseDTO(found.getUserId(), found.getUsername(), found.getAvatarURL());
    }
}
