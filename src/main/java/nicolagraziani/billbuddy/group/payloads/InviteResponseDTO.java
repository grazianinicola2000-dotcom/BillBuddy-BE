package nicolagraziani.billbuddy.group.payloads;

import nicolagraziani.billbuddy.group.enums.InviteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InviteResponseDTO(
        UUID inviteId,
        UUID groupId,
        String groupName,
        UUID invitedById,
        String invitedByUsername,
        UUID receiverId,
        String receiverUsername,
        InviteStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiredAt
) {
}
