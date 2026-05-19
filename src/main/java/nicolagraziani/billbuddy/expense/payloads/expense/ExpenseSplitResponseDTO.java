package nicolagraziani.billbuddy.expense.payloads.expense;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseSplitResponseDTO(
        UUID expenseSplitId,
        UUID userId,
        String username,
        String avatarUrl,
        BigDecimal amountOwed,
        BigDecimal amountPaid
) {
}
