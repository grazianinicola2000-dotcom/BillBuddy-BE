package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record UserGroupBalanceDTO(
        UUID userId,
        String username,
        BigDecimal totalSpent,
        BigDecimal totalPaidBack,
        BigDecimal totalOwed,
        BigDecimal totalToReceive,
        BigDecimal netBalance,
        BigDecimal currentExposure,
        CurrencyCode currencyCode
) {
}
