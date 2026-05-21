package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitBalanceDTO(
        UUID expenseId,
        String expenseTitle,
        UUID expenseSplitId,
        UUID debtorId,
        String debtorUsername,
        UUID creditorId,
        String creditorUsername,
        BigDecimal originalAmount,
        BigDecimal amountPaid,
        BigDecimal remainingDebt,
        CurrencyCode currencyCode,
        boolean settled
) {
}
