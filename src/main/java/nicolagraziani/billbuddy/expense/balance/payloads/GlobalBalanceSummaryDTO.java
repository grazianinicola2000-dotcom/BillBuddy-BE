package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record GlobalBalanceSummaryDTO(
        UUID userId,
        String username,
        CurrencyCode currencyCode,
        BigDecimal totalSpent,
        BigDecimal totalReceived,
        BigDecimal currentExposure,
        BigDecimal totalOwed,
        BigDecimal totalToReceive,
        BigDecimal netBalance
) {
}
