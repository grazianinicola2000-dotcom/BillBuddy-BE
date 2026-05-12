package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.BadRequestException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.Invite;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.group.payloads.InviteResponseDTO;
import nicolagraziani.billbuddy.group.repositories.InviteRepository;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.stereotype.Service;

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

    public InviteResponseDTO createInvite(Group group, User invitedBy, User receiver) {
        Group foundGroup = this.groupService.findGroupById(group.getGroupId());
        User foundInvitedBy = this.userService.findUserById(invitedBy.getUserId());
        User foundReceiver = this.userService.findUserById(receiver.getUserId());

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
}
