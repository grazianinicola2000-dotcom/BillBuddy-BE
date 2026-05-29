package nicolagraziani.billbuddy.config;

import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.expense.payloads.expenseCategory.CreateExpenseCategoryDTO;
import nicolagraziani.billbuddy.expense.services.ExpenseCategoryService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpenseCategoryRunner implements CommandLineRunner {

    private final ExpenseCategoryService expenseCategoryService;

    public ExpenseCategoryRunner(ExpenseCategoryService expenseCategoryService) {
        this.expenseCategoryService = expenseCategoryService;
    }

    @Override
    public void run(String... args) {
        List<String> categories = List.of(
                "Food",
                "Groceries",
                "Rent",
                "Utilities",
                "Transport",
                "Entertainment",
                "Travel",
                "Health",
                "Shopping",
                "Education",
                "Other"
        );
        categories.forEach(this::createIfMissing);
    }

    private void createIfMissing(String name) {
        try {
            expenseCategoryService.createCategory(new CreateExpenseCategoryDTO(name, null));
        } catch (BadRequestException ignored) {
        }
    }

}
