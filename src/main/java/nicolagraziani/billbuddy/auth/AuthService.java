package nicolagraziani.billbuddy.auth;

import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.exceptions.UnauthorizedException;
import nicolagraziani.billbuddy.security.TokenTools;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final TokenTools tokenTools;
    private final UserService userService;
    private final PasswordEncoder bcrypt;

    public AuthService(TokenTools tokenTools, UserService userService, PasswordEncoder bcrypt) {
        this.tokenTools = tokenTools;
        this.userService = userService;
        this.bcrypt = bcrypt;
    }

    public String checkCredentialAndGenerateToken(LoginDTO body) {

        try {
            User found = this.userService.findByEmail(body.email().toLowerCase());
            if (!found.isActive()) {
                throw new UnauthorizedException("Account disabled");
            }
            if (this.bcrypt.matches(body.password(), found.getPassword())) {
                return this.tokenTools.generateToken(found);
            } else {
                throw new UnauthorizedException("Invalid credentials. Please try again.");
            }
        } catch (NotFoundException ex) {
            throw new UnauthorizedException("Invalid credentials. Please try again.");
        }
    }
}
