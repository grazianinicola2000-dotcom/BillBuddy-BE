package nicolagraziani.billbuddy.group.payloads;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GroupDetailsResponseDTO(
        UUID groupId,
        String name,
        String description,
        String imageUrl,
        LocalDateTime createdAt,
        int memberCount,
        List<GroupMemberResponseDTO> members

) {
}
