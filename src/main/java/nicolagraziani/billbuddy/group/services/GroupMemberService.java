package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.expense.entities.ExpenseSplit;
import nicolagraziani.billbuddy.expense.services.ExpenseSplitService;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.GroupMember;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.group.payloads.GroupMemberResponseDTO;
import nicolagraziani.billbuddy.group.repositories.GroupMemberRepository;
import nicolagraziani.billbuddy.group.repositories.GroupRepository;
import nicolagraziani.billbuddy.user.Role;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GroupMemberService {
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserService userService;
    private final ExpenseSplitService expenseSplitService;

    public GroupMemberService(GroupMemberRepository groupMemberRepository, GroupRepository groupRepository, UserService userService, ExpenseSplitService expenseSplitService) {
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.expenseSplitService = expenseSplitService;
    }

    public GroupMemberResponseDTO saveGroupMember(User user, Group group, GroupRole role) {
        if (this.existsByGroupAndUser(group, user)) {
            throw new BadRequestException("User is already a member of this group");
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

    private Group findGroupById(UUID groupId) {
        return this.groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException(groupId));
    }

    public boolean isOwnerOrSystemAdmin(Group group, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        GroupMember membership = this.findByGroupAndUser(group, user);
        return membership.getRole() == GroupRole.OWNER;
    }

    @Transactional
    public void removeMember(UUID groupId, UUID userId, User currentUser) {
        Group foundGroup = this.findGroupById(groupId);
        User foundCurrentUser = this.userService.findActiveUserById(currentUser.getUserId());
        User foundTargetUser = this.userService.findActiveUserById(userId);
        GroupMember targetMembership = this.findByGroupAndUser(foundGroup, foundTargetUser);

        boolean isSystemAdmin = foundCurrentUser.getRole() == Role.ADMIN;

        if (!this.isOwnerOrSystemAdmin(foundGroup, foundCurrentUser)) {
            throw new AuthorizationDeniedException("You are not allowed to manage group members");
        }
        if (targetMembership.getRole() == GroupRole.OWNER && !isSystemAdmin) {
            throw new BadRequestException("You cannot remove the group owner");
        }
        if (foundCurrentUser.getUserId().equals(foundTargetUser.getUserId()) && !isSystemAdmin) {
            throw new BadRequestException("Owners cannot remove themselves from the group");
        }
        if (this.userHasOpenBalances(foundGroup, userId)) {
            throw new BadRequestException("This member has open debts or credits and cannot be removed from the group");
        }

        this.groupMemberRepository.delete(targetMembership);
        log.info("User {} removed {} from group {}",
                foundCurrentUser.getUsername(),
                foundTargetUser.getUsername(),
                foundGroup.getName());
    }

    @Transactional
    public void promoteToAdmin(UUID groupId, UUID userId, User currentUser) {
        Group foundGroup = this.findGroupById(groupId);
        User foundCurrentUser = this.userService.findActiveUserById(currentUser.getUserId());
        User foundTargetUser = this.userService.findActiveUserById(userId);

        GroupMember targetMembership = this.findByGroupAndUser(foundGroup, foundTargetUser);

        if (!this.isOwnerOrSystemAdmin(foundGroup, foundCurrentUser)) {
            throw new AuthorizationDeniedException("You are not allowed to promote group members");
        }
        if (targetMembership.getRole() != GroupRole.MEMBER) {
            throw new BadRequestException("User is already an admin or owner");
        }

        targetMembership.setRole(GroupRole.ADMIN);
        this.groupMemberRepository.save(targetMembership);
        log.info("User {} promoted {} to ADMIN in group {}",
                foundCurrentUser.getUsername(),
                foundTargetUser.getUsername(),
                foundGroup.getName());
    }

    @Transactional
    public void demoteToMember(UUID groupId, UUID userId, User currentUser) {
        Group foundGroup = this.findGroupById(groupId);
        User foundCurrentUser = this.userService.findActiveUserById(currentUser.getUserId());
        User foundTargetUser = this.userService.findActiveUserById(userId);

        GroupMember targetMembership = this.findByGroupAndUser(foundGroup, foundTargetUser);

        if (!this.isOwnerOrSystemAdmin(foundGroup, foundCurrentUser)) {
            throw new AuthorizationDeniedException("You are not allowed to demote group members");
        }
        if (targetMembership.getRole() == GroupRole.MEMBER) {
            throw new BadRequestException("User is already a member");
        }
        if (targetMembership.getRole() == GroupRole.OWNER) {
            throw new BadRequestException("You cannot demote the group owner");
        }

        targetMembership.setRole(GroupRole.MEMBER);
        this.groupMemberRepository.save(targetMembership);
        log.info("User {} demoted {} to MEMBER in group {}",
                foundCurrentUser.getUsername(),
                foundTargetUser.getUsername(),
                foundGroup.getName());
    }

    //    VALIDATE MEMBERSHIP
    public void validateMembership(Group group, User user) {
        if (!this.existsByGroupAndUser(group, user)) {
            throw new AuthorizationDeniedException("You are not a member of this group");
        }
    }

    private boolean userHasOpenBalances(Group group, UUID userId) {
        List<ExpenseSplit> splits = this.expenseSplitService.findExpenseSplitByGroup(group);
        return splits.stream().anyMatch(split -> {
            BigDecimal remainingDebt = split.getAmountOwed().subtract(split.getAmountPaid());
            if (remainingDebt.compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }

            UUID debtorId = split.getUser().getUserId();
            UUID creditorId = split.getExpense().getPaidBy().getUserId();
            return debtorId.equals(userId) || creditorId.equals(userId);
        });
    }
}
