package nicolagraziani.billbuddy.expense.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseCategory;
import nicolagraziani.billbuddy.expense.repositories.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class ExpenseCategoryService {
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public ExpenseCategoryService(ExpenseCategoryRepository expenseCategoryRepository) {
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    public ExpenseCategory findExpenseCategoryById(UUID categoryId) {
        return this.expenseCategoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException(categoryId));
    }
}
