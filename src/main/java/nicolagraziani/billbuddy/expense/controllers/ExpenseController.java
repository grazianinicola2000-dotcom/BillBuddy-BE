package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.expense.payloads.CreateExpenseDTO;
import nicolagraziani.billbuddy.expense.payloads.ExpenseResponseDTO;
import nicolagraziani.billbuddy.expense.payloads.GetExpensesFilterDTO;
import nicolagraziani.billbuddy.expense.services.ExpenseService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponseDTO getExpenseById(@PathVariable UUID expenseId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.expenseService.getExpenseById(expenseId, currentAuthenticatedUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponseDTO createExpense(@AuthenticationPrincipal User currentAuthenticatedUser, @Validated @RequestBody CreateExpenseDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }
        return this.expenseService.createExpense(body, currentAuthenticatedUser);
    }

    @GetMapping("/me/paid")
    public Page<ExpenseResponseDTO> findExpensesPaidByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            GetExpensesFilterDTO filters,
            @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.expenseService.findExpensesPaidByUser(page, size, sortBy, currentAuthenticatedUser, filters);
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable UUID expenseId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.expenseService.deleteExpense(expenseId, currentAuthenticatedUser);
    }
}
