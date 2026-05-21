package nicolagraziani.billbuddy.expense.balance.payloads;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GroupBalanceSummaryDTO(
        UUID groupId,
        String groupName,
        CurrencyCode currencyCode,
        BigDecimal totalExpenses,
        BigDecimal totalSettled,
        BigDecimal totalOutstanding,
        List<NetBalanceDTO> netBalances,
        List<OptimizedPaymentDTO> optimizedPayments
) {
}
