package nicolagraziani.billbuddy.user;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record UserDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,
        @NotBlank(message = "Surname is required")
        @Size(min = 2, max = 50, message = "Surname must be between 2 and 50 characters")
        String surname,
        @NotBlank(message = "Username is required")
        @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters")
        String username,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must contain at least 6 characters")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        String password,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth
) {

}
