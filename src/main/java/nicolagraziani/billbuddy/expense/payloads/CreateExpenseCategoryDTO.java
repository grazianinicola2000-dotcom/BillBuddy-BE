package nicolagraziani.billbuddy.expense.payloads;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateExpenseCategoryDTO(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name cannot exceed 50 characters")
        String name,
        @Size(max = 500, message = "Icon URL cannot exceed 500 characters")
        String icon
) {
}
