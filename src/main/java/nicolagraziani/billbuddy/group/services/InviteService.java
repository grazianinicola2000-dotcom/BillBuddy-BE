package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.Invite;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.group.payloads.InviteResponseDTO;
import nicolagraziani.billbuddy.group.repositories.InviteRepository;
import nicolagraziani.billbuddy.user.Role;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class InviteService {

    private final InviteRepository inviteRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    public InviteService(InviteRepository inviteRepository, UserService userService, GroupService groupService, GroupMemberService groupMemberService) {
        this.inviteRepository = inviteRepository;
        this.userService = userService;
        this.groupService = groupService;
        this.groupMemberService = groupMemberService;
    }

    @Transactional
    public InviteResponseDTO createInvite(Group group, User invitedBy, User receiver) {
        Group foundGroup = this.groupService.findGroupById(group.getGroupId());
        User foundInvitedBy = this.userService.findActiveUserById(invitedBy.getUserId());
        User foundReceiver = this.userService.findActiveUserById(receiver.getUserId());

        if (!this.groupMemberService.isAdminOrOwner(foundGroup, foundInvitedBy)) {
            throw new AuthorizationDeniedException("Only group admins or owners can send invites");
        }
        if (this.inviteRepository.existsByGroupAndReceiverAndStatus(foundGroup, foundReceiver, InviteStatus.PENDING)) {
            throw new BadRequestException("A pending invite already exists for this user");
        }
        if (this.groupMemberService.existsByGroupAndUser(foundGroup, foundReceiver)) {
            throw new BadRequestException("User is already a member of this group");
        }
        if (foundInvitedBy.getUserId().equals(foundReceiver.getUserId())) {
            throw new BadRequestException("You cannot invite yourself");
        }

        Invite newInvite = new Invite(foundGroup, foundInvitedBy, foundReceiver);
        this.inviteRepository.save(newInvite);

        log.info("User {} invited {} to group {}",
                foundInvitedBy.getUsername(),
                foundReceiver.getUsername(),
                foundGroup.getName());

        return new InviteResponseDTO(
                newInvite.getInviteId(),
                foundGroup.getGroupId(),
                foundGroup.getName(),
                foundInvitedBy.getUserId(),
                foundInvitedBy.getUsername(),
                foundReceiver.getUserId(),
                foundReceiver.getUsername(),
                newInvite.getStatus(),
                newInvite.getCreatedAt(),
                newInvite.getExpiresAt()
        );
    }

    public Invite findInviteById(UUID inviteId) {
        return this.inviteRepository.findById(inviteId).orElseThrow(() -> new NotFoundException(inviteId));
    }

    private void validatePendingInvite(Invite invite) {
        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new BadRequestException("Invite is no longer pending");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            invite.setStatus(InviteStatus.EXPIRED);
            this.inviteRepository.save(invite);
            throw new BadRequestException("Invite has expired");
        }
    }

    @Transactional
    public InviteResponseDTO acceptInvite(UUID inviteId, User user) {
        Invite foundInvite = this.findInviteById(inviteId);
        User foundUser = this.userService.findActiveUserById(user.getUserId());

        if (!foundInvite.getReceiver().getUserId().equals(foundUser.getUserId())) {
            throw new AuthorizationDeniedException("You are not allowed to accept this invite");
        }
        this.validatePendingInvite(foundInvite);
        if (this.groupMemberService.existsByGroupAndUser(foundInvite.getGroup(), foundUser)) {
            throw new BadRequestException("User is already a member of this group");
        }

        this.groupMemberService.saveGroupMember(foundUser, foundInvite.getGroup(), GroupRole.MEMBER);

        foundInvite.setStatus(InviteStatus.ACCEPTED);
        Invite savedInvite = this.inviteRepository.save(foundInvite);

        log.info("User {} accepted invite for group {}",
                foundUser.getUsername(),
                foundInvite.getGroup().getName());

        return new InviteResponseDTO(
                savedInvite.getInviteId(),
                savedInvite.getGroup().getGroupId(),
                savedInvite.getGroup().getName(),
                savedInvite.getInvitedBy().getUserId(),
                savedInvite.getInvitedBy().getUsername(),
                savedInvite.getReceiver().getUserId(),
                savedInvite.getReceiver().getUsername(),
                savedInvite.getStatus(),
                savedInvite.getCreatedAt(),
                savedInvite.getExpiresAt()
        );
    }

    @Transactional
    public void declineInvite(UUID inviteId, User user) {
        Invite foundInvite = this.findInviteById(inviteId);
        User foundUser = this.userService.findActiveUserById(user.getUserId());

        if (!foundInvite.getReceiver().getUserId().equals(foundUser.getUserId())) {
            throw new AuthorizationDeniedException("You are not allowed to decline this invite");
        }
        this.validatePendingInvite(foundInvite);

        foundInvite.setStatus(InviteStatus.DECLINED);
        this.inviteRepository.save(foundInvite);

        log.info("User {} declined invite for group {}",
                foundUser.getUsername(),
                foundInvite.getGroup().getName());
    }

    @Transactional
    public void cancelInvite(UUID inviteId, User user) {
        Invite foundInvite = this.findInviteById(inviteId);
        User foundUser = this.userService.findActiveUserById(user.getUserId());

        boolean isSender = foundInvite.getInvitedBy().getUserId().equals(foundUser.getUserId());
        boolean isAdminOrOwner = this.groupMemberService.isAdminOrOwner(foundInvite.getGroup(), foundUser);
        boolean isSystemAdmin = foundUser.getRole() == Role.ADMIN;

        if (!isSender && !isAdminOrOwner && !isSystemAdmin) {
            throw new AuthorizationDeniedException("You are not allowed to cancel this invite");
        }

        this.validatePendingInvite(foundInvite);
        foundInvite.setStatus(InviteStatus.CANCELLED);
        this.inviteRepository.save(foundInvite);
        log.info("Invite {} cancelled by user {}",
                foundInvite.getInviteId(),
                foundUser.getUsername());
    }

    public Page<InviteResponseDTO> findMyInvites(int page, int size, String sortBy, User user, InviteStatus status) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        User foundUser = this.userService.findActiveUserById(user.getUserId());

        Page<Invite> invites;

        if (status != null) {
            invites = this.inviteRepository.findByReceiverAndStatus(foundUser, pageable, status);
        } else {
            invites = this.inviteRepository.findByReceiver(foundUser, pageable);
        }

        return invites.map(
                invite -> new InviteResponseDTO(
                        invite.getInviteId(),
                        invite.getGroup().getGroupId(),
                        invite.getGroup().getName(),
                        invite.getInvitedBy().getUserId(),
                        invite.getInvitedBy().getUsername(),
                        invite.getReceiver().getUserId(),
                        invite.getReceiver().getUsername(),
                        invite.getStatus(),
                        invite.getCreatedAt(),
                        invite.getExpiresAt()
                )
        );
    }

    public Page<InviteResponseDTO> findSentInvites(int page, int size, String sortBy, User user, InviteStatus status) {
        if (size > 100 || size < 1) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());

        User foundUser = this.userService.findActiveUserById(user.getUserId());
        Page<Invite> invites;

        if (status != null) {
            invites = this.inviteRepository.findByInvitedByAndStatus(foundUser, status, pageable);
        } else {
            invites = this.inviteRepository.findByInvitedBy(foundUser, pageable);
        }

        return invites.map(
                invite -> new InviteResponseDTO(
                        invite.getInviteId(),
                        invite.getGroup().getGroupId(),
                        invite.getGroup().getName(),
                        invite.getInvitedBy().getUserId(),
                        invite.getInvitedBy().getUsername(),
                        invite.getReceiver().getUserId(),
                        invite.getReceiver().getUsername(),
                        invite.getStatus(),
                        invite.getCreatedAt(),
                        invite.getExpiresAt()
                )
        );
    }
}
