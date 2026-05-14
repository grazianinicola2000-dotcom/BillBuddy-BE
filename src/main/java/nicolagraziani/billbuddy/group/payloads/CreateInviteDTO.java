package nicolagraziani.billbuddy.group.payloads;

import jakarta.validation.constraints.NotNull;
import nicolagraziani.billbuddy.group.enums.GroupRole;

import java.util.UUID;

public record CreateInviteDTO(
        @NotNull(message = "Receiver id is required")
        UUID receiverId,
        GroupRole role
) {
}
