package nicolagraziani.billbuddy.user;

import java.time.LocalDate;
import java.util.UUID;

public record UserResponseDTO(
        UUID userId,
        String name,
        String surname,
        String username,
        String email,
        LocalDate dateOfBirth,
        String avatarUrl,
        Role role) {
}
