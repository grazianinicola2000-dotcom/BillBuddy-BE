package nicolagraziani.billbuddy.expense.repositories;

import nicolagraziani.billbuddy.expense.entities.Expense;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    Page<Expense> findByGroup(Group group, Pageable pageable);

    Page<Expense> findByPaidBy(User user, Pageable pageable);

    Page<Expense> findByGroupAndExpenseType(Group group, ExpenseType expenseType, Pageable pageable);

    Page<Expense> findByPaidByAndExpenseType(User user, ExpenseType expenseType, Pageable pageable);
}
