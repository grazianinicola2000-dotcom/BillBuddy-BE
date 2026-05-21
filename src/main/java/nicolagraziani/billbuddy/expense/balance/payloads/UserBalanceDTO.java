package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record UserBalanceDTO(
        UUID debtorId,
        String debtorUsername,
        UUID creditorId,
        String creditorUsername,
        BigDecimal amount,
        CurrencyCode currencyCode
) {
}
