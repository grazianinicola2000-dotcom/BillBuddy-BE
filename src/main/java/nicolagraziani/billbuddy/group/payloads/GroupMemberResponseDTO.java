package nicolagraziani.billbuddy.group.payloads;

import nicolagraziani.billbuddy.group.enums.GroupRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupMemberResponseDTO(
        UUID groupMemberId,
        UUID userId,
        String username,
        String avatarUrl,
        GroupRole role,
        LocalDateTime joinedAt
) {
}
