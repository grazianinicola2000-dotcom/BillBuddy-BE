package nicolagraziani.billbuddy.group.repositories;

import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.GroupMember;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroupAndUser(Group group, User user);

    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    List<GroupMember> findAllByGroup(Group group);

    Page<GroupMember> findByUser(User user, Pageable pageable);

    void deleteAllByGroup(Group group);
}
