package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.GroupMember;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.group.payloads.GroupMemberResponseDTO;
import nicolagraziani.billbuddy.group.repositories.GroupMemberRepository;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;

    public GroupMemberService(GroupMemberRepository groupMemberRepository) {
        this.groupMemberRepository = groupMemberRepository;
    }

    public GroupMemberResponseDTO saveGroupMember(User user, Group group, GroupRole role) {
        if (this.existsByGroupAndUser(group, user)) {
            throw new BadRequestException("This user already exists in the group");
        }
        GroupMember newGroupMember = new GroupMember(user, group, role);
        GroupMember savedGroupMember = this.groupMemberRepository.save(newGroupMember);
        log.info("User {} joined group {} with role {}",
                user.getEmail(),
                group.getName(),
                role);
        return new GroupMemberResponseDTO(savedGroupMember.getGroupMemberId(), user.getUserId(), user.getUsername(), user.getAvatarURL(), savedGroupMember.getRole(), savedGroupMember.getJoinedAt());
    }

    public boolean existsByGroupAndUser(Group group, User user) {
        return this.groupMemberRepository.existsByGroupAndUser(group, user);
    }

    public GroupMember findByGroupAndUser(Group group, User user) {
        return this.groupMemberRepository.findByGroupAndUser(group, user).orElseThrow(() -> new NotFoundException("User is not a member of this group"));
    }

    public boolean isAdminOrOwner(Group group, User user) {
        GroupMember membership = this.findByGroupAndUser(group, user);
        return membership.getRole() == GroupRole.OWNER
                || membership.getRole() == GroupRole.ADMIN;
    }

    public Page<GroupMember> findByUser(User user, Pageable pageable) {
        return this.groupMemberRepository.findByUser(user, pageable);
    }

    public List<GroupMember> findAllByGroup(Group group) {
        return this.groupMemberRepository.findAllByGroup(group);
    }

    @Transactional
    public void deleteAllByGroup(Group group) {
        this.groupMemberRepository.deleteAllByGroup(group);
    }
}
