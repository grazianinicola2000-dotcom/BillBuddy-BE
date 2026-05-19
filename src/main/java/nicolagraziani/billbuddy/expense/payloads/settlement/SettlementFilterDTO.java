package nicolagraziani.billbuddy.expense.payloads.settlement;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.util.UUID;

public record SettlementFilterDTO(
        CurrencyCode currencyCode,
        UUID groupId
) {
}
