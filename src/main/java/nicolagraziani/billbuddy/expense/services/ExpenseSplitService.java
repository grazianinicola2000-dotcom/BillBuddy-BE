package nicolagraziani.billbuddy.expense.services;

import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.repositories.ExpenseSplitRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ExpenseSplitService {
    private final ExpenseSplitRepository expenseSplitRepository;

    public ExpenseSplitService(ExpenseSplitRepository expenseSplitRepository) {
        this.expenseSplitRepository = expenseSplitRepository;
    }

    public ExpenseSplit findExpenseSplitById(UUID splitId) {
        return this.expenseSplitRepository.findById(splitId).orElseThrow(() -> new NotFoundException(splitId));
    }
}
