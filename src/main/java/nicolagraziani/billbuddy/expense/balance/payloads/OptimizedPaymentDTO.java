package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record OptimizedPaymentDTO(
        UUID formUserId,
        String fromUsername,
        UUID toUserId,
        String toUsername,
        BigDecimal amount,
        CurrencyCode currencyCode
) {
}
