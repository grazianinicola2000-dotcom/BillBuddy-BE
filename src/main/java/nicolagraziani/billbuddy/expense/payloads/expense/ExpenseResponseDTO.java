package nicolagraziani.billbuddy.expense.payloads.expense;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseResponseDTO(
        UUID expenseId,
        String title,
        String description,
        BigDecimal totalAmount,
        ExpenseType expenseType,
        CurrencyCode currencyCode,
        LocalDate expenseDate,
        UUID paidById,
        String paidByUsername,
        UUID groupId,
        String groupName,
        UUID categoryId,
        String categoryName,
        List<ExpenseSplitResponseDTO> splits,
        LocalDateTime createdAt
) {
}
