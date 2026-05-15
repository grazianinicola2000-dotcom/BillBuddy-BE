package nicolagraziani.billbuddy.expense.repositories;

import nicolagraziani.billbuddy.expense.entities.Settlement;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    List<Settlement> findByPayer(User payer);

    List<Settlement> findByReceiver(User receiver);

    List<Settlement> findByGroup(Group group);
}
