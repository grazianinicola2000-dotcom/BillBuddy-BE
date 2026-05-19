package nicolagraziani.billbuddy.expense.payloads.settlement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSettlementDTO(
        @NotNull(message = "Expense split id is required")
        UUID expenseSplitId,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Amount cannot have more than 2 decimal places")
        BigDecimal amount,
        @NotNull(message = "Currency code is required")
        CurrencyCode currencyCode,
        @Size(max = 255, message = "Note cannot exceed 255 characters")
        String note
) {
}
