package nicolagraziani.billbuddy.expense.payloads.settlement;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SettlementResponseDTO(
        UUID settlementId,
        UUID payerId,
        String payerUsername,
        UUID receiverId,
        String receiverUsername,
        UUID groupId,
        String groupName,
        BigDecimal amount,
        CurrencyCode currencyCode,
        String note,
        LocalDateTime createdAt) {
}
