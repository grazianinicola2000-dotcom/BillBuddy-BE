package nicolagraziani.billbuddy.expense.repositories;

import nicolagraziani.billbuddy.expense.entities.Expense;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {

    List<ExpenseSplit> findByExpense(Expense expense);

    List<ExpenseSplit> findByUser(User user);

    boolean existsByExpenseAndUser(Expense expense, User user);

    List<ExpenseSplit> findByExpense_Group(Group group);

    List<ExpenseSplit> findByExpense_PaidBy(User user);
}
