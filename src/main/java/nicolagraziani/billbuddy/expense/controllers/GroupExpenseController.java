package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.expense.payloads.expense.ExpenseResponseDTO;
import nicolagraziani.billbuddy.expense.payloads.expense.GetExpensesFilterDTO;
import nicolagraziani.billbuddy.expense.services.ExpenseService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupExpenseController {

    private final ExpenseService expenseService;

    public GroupExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{groupId}/expenses")
    public Page<ExpenseResponseDTO> findGroupExpenses(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            GetExpensesFilterDTO filters,
            @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.expenseService.findGroupExpenses(page, size, sortBy, currentAuthenticatedUser, filters, groupId);
    }
}
