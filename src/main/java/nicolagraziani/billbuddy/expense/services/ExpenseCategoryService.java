package nicolagraziani.billbuddy.expense.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseCategory;
import nicolagraziani.billbuddy.expense.payloads.expenseCategory.CreateExpenseCategoryDTO;
import nicolagraziani.billbuddy.expense.payloads.expenseCategory.ExpenseCategoryResponseDTO;
import nicolagraziani.billbuddy.expense.repositories.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    public List<ExpenseCategoryResponseDTO> getAllCategories() {
        return this.expenseCategoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public ExpenseCategoryResponseDTO createCategory(CreateExpenseCategoryDTO body) {
        if (this.expenseCategoryRepository.existsByNameIgnoreCase(body.name())) {
            throw new BadRequestException("A category with this name already exists");
        }

        ExpenseCategory category = new ExpenseCategory(body.name());

        if (body.icon() != null && !body.icon().isBlank()) {
            category.setIcon(body.icon());
        }

        ExpenseCategory savedCategory = this.expenseCategoryRepository.save(category);

        log.info("Expense category '{}' created", savedCategory.getName());
        return this.mapToResponse(savedCategory);
    }

    private ExpenseCategoryResponseDTO mapToResponse(ExpenseCategory category) {
        return new ExpenseCategoryResponseDTO(category.getExpenseCategoryId(), category.getName(), category.getIcon());
    }

    public ExpenseCategoryResponseDTO findCategoryByName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("Category name cannot be blank");
        }
        ExpenseCategory category = this.expenseCategoryRepository.findByNameIgnoreCase(name).orElseThrow(() -> new NotFoundException("Expense category not found"));
        return this.mapToResponse(category);
    }
}
