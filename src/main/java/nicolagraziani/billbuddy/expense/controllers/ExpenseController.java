package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.expense.payloads.CreateExpenseDTO;
import nicolagraziani.billbuddy.expense.payloads.ExpenseResponseDTO;
import nicolagraziani.billbuddy.expense.services.ExpenseService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
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
}
