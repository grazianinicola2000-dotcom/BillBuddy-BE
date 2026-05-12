package nicolagraziani.billbuddy.auth;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.user.UserDTO;
import nicolagraziani.billbuddy.user.UserResponseDTO;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginDTO body) {
        return new LoginResponseDTO(this.authService.checkCredentialAndGenerateToken(body));
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO saveUser(@RequestBody @Validated UserDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }

        return this.userService.saveUser(body);
    }
}
