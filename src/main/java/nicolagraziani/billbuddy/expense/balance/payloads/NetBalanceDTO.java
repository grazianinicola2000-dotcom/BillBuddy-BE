package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record NetBalanceDTO(
        UUID userId,
        String username,
        BigDecimal netBalance,
        CurrencyCode currencyCode
) {
}
