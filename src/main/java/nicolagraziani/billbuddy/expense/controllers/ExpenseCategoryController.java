package nicolagraziani.billbuddy.expense.controllers;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.expense.payloads.CreateExpenseCategoryDTO;
import nicolagraziani.billbuddy.expense.payloads.ExpenseCategoryResponseDTO;
import nicolagraziani.billbuddy.expense.services.ExpenseCategoryService;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense-categories")
public class ExpenseCategoryController {

    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseCategoryController(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @GetMapping
    public List<ExpenseCategoryResponseDTO> getAllCategories() {
        return this.expenseCategoryService.getAllCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ADMIN')")
    public ExpenseCategoryResponseDTO createCategory(@Validated @RequestBody CreateExpenseCategoryDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }
        return this.expenseCategoryService.createCategory(body);
    }

    @GetMapping("/search")
    public ExpenseCategoryResponseDTO findCategoryByName(@RequestParam String name) {
        return this.expenseCategoryService.findCategoryByName(name);
    }
}
