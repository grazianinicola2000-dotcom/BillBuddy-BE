package nicolagraziani.billbuddy.expense.repositories;

import nicolagraziani.billbuddy.expense.entities.Expense;
import nicolagraziani.billbuddy.expense.entities.Settlement;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    Page<Settlement> findByDebtor(User Debtor, Pageable pageable);

    Page<Settlement> findByCreditor(User creditor, Pageable pageable);

    List<Settlement> findByCreditor(User creditor);

    Page<Settlement> findByGroup(Group group, Pageable pageable);

    List<Settlement> findByGroup(Group group);

    Page<Settlement> findByGroupAndCurrencyCode(Group group, CurrencyCode currencyCode, Pageable pageable);

    Page<Settlement> findByDebtorAndCurrencyCode(User debtor, CurrencyCode currencyCode, Pageable pageable);

    boolean existsByExpenseSplit_Expense(Expense expense);
}
