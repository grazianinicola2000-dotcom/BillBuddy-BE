package nicolagraziani.billbuddy.expense.repositories;

import nicolagraziani.billbuddy.expense.entities.Settlement;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    Page<Settlement> findByPayer(User payer, Pageable pageable);

    Page<Settlement> findByReceiver(User receiver, Pageable pageable);

    Page<Settlement> findByGroup(Group group, Pageable pageable);

    Page<Settlement> findByGroupAndCurrencyCode(Group group, CurrencyCode currencyCode, Pageable pageable);

    Page<Settlement> findByPayerAndCurrencyCode(User payer, CurrencyCode currencyCode, Pageable pageable);
}
