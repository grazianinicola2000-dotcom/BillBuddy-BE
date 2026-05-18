package nicolagraziani.billbuddy.expense.payloads;

import java.math.BigDecimal;
import java.util.UUID;

public record ExpenseSplitResponseDTO(
        UUID userId,
        String username,
        String avatarUrl,
        BigDecimal amountOwed) {
}
