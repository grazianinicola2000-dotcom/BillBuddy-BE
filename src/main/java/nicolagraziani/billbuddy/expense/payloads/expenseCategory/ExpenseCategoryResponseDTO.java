package nicolagraziani.billbuddy.expense.payloads.expenseCategory;

import java.util.UUID;

public record ExpenseCategoryResponseDTO(
        UUID expenseCategoryId,
        String name,
        String icon
) {
}
