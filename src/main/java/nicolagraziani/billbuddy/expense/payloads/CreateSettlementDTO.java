package nicolagraziani.billbuddy.expense.payloads;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSettlementDTO(
        @NotNull(message = "Receiver id is required")
        UUID receiverId,
        UUID groupId,
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,
        @NotNull(message = "Currency code is required")
        CurrencyCode currencyCode,
        @Size(max = 255, message = "Note cannot exceed 255 characters")
        String note

) {
}
