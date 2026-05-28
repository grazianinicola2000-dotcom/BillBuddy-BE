package nicolagraziani.billbuddy.group.repositories;

import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.Invite;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Invite> findByReceiver(User receiver, Pageable pageable);

    Page<Invite> findByReceiverAndStatus(User receiver, Pageable pageable, InviteStatus status);

    Page<Invite> findByInvitedByAndStatus(User invitedBy, InviteStatus status, Pageable pageable);

    Page<Invite> findByInvitedBy(User invitedBy, Pageable pageable);
}
