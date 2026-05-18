package nicolagraziani.billbuddy.group.payloads;

import jakarta.validation.constraints.NotBlank;

public record CreateInviteDTO(
        @NotBlank(message = "Username is required")
        String username
) {
}
