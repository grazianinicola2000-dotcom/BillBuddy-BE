package nicolagraziani.billbuddy.expense.services;

import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.repositories.ExpenseSplitRepository;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<ExpenseSplit> findExpenseSplitByGroup(Group group) {
        return this.expenseSplitRepository.findByExpense_Group(group);
    }

    public List<ExpenseSplit> findExpenseSplitByUser(User user) {
        return this.expenseSplitRepository.findByUser(user);
    }

    public List<ExpenseSplit> findExpenseSplitByCreditor(User user) {
        return this.expenseSplitRepository.findByExpense_PaidBy(user);
    }
}
