package nicolagraziani.billbuddy.group.payloads;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponseDTO(
        UUID groupId,
        String name,
        String description,
        String imageUrl,
        LocalDateTime createdAt
) {
}
