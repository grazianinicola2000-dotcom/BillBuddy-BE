package nicolagraziani.billbuddy.group.repositories;

import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.Invite;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InviteRepository extends JpaRepository<Invite, UUID> {

    boolean existsByGroupAndReceiverAndStatus(
            Group group,
            User receiver,
            InviteStatus status
    );
}
