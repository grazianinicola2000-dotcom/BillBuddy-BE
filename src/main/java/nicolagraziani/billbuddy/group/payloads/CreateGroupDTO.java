package nicolagraziani.billbuddy.group.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupDTO(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description
) {
}
