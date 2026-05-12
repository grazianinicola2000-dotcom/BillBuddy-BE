package nicolagraziani.billbuddy.group.repositories;

import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.GroupMember;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupAndUser(Group group, User user);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);
}
