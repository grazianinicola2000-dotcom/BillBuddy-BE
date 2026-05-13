package nicolagraziani.billbuddy.user;

import java.util.UUID;

public record PublicUserResponseDTO(
        UUID userId,
        String username,
        String avatarUrl) {
}
