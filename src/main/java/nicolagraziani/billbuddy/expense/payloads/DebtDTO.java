package nicolagraziani.billbuddy.expense.payloads;

import java.math.BigDecimal;
import java.util.UUID;

public record DebtDTO(
        UUID fromUserId,
        String fromUsername,
        UUID toUserId,
        String toUsername,
        BigDecimal amount
) {
}
