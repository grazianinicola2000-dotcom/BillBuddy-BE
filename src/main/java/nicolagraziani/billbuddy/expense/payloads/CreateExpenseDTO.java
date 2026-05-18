package nicolagraziani.billbuddy.expense.payloads;

import jakarta.validation.constraints.*;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateExpenseDTO(
        @NotBlank(message = "Title is required")
        @Size(max = 50, message = "Title cannot exceed 50 characters")
        String title,
        @Size(max = 255, message = "Description cannot exceed 255 characters")
        String description,
        @NotNull(message = "Total amount is required")
        @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
        BigDecimal totalAmount,
        UUID groupId,
        UUID categoryId,
        @NotNull(message = "Currency code is required")
        CurrencyCode currencyCode,
        @NotNull(message = "Expense date is required")
        @PastOrPresent(message = "expenseDate cannot be in the future")
        LocalDate expenseDate,
        List<UUID> participantIds

) {
}
