package nicolagraziani.billbuddy.expense.payloads.expense;

import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;

import java.util.UUID;

public record GetExpensesFilterDTO(
        ExpenseType expenseType,
        CurrencyCode currencyCode,
        UUID groupId,
        UUID categoryId
) {
}
