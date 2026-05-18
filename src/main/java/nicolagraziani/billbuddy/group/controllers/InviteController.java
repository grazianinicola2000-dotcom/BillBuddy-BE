package nicolagraziani.billbuddy.group.controllers;

import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.group.payloads.CreateInviteDTO;
import nicolagraziani.billbuddy.group.payloads.InviteResponseDTO;
import nicolagraziani.billbuddy.group.services.GroupService;
import nicolagraziani.billbuddy.group.services.InviteService;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class InviteController {

    private final InviteService inviteService;
    private final GroupService groupService;
    private final UserService userService;

    public InviteController(InviteService inviteService, GroupService groupService, UserService userService) {
        this.inviteService = inviteService;
        this.groupService = groupService;
        this.userService = userService;
    }

    @PostMapping("/groups/{groupId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponseDTO createInvite(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser, @RequestBody @Validated CreateInviteDTO body, BindingResult validation) {
        Group foundGroup = this.groupService.findGroupById(groupId);
        User receiver = this.userService.findByUsernameAndIsActive(body.username());

        return this.inviteService.createInvite(foundGroup, currentAuthenticatedUser, receiver);
    }

    @PatchMapping("/invites/{inviteId}/accept")
    public InviteResponseDTO acceptInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.inviteService.acceptInvite(inviteId, currentAuthenticatedUser);
    }

    @PatchMapping("/invites/{inviteId}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void declineInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.inviteService.declineInvite(inviteId, currentAuthenticatedUser);
    }

    @PatchMapping("/invites/{inviteId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelInvite(@PathVariable UUID inviteId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.inviteService.cancelInvite(inviteId, currentAuthenticatedUser);
    }

    @GetMapping("/invites/me")
    public Page<InviteResponseDTO> findMyInvites(@AuthenticationPrincipal User currentAuthenticatedUser,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                                 @RequestParam(required = false) InviteStatus status) {
        return this.inviteService.findMyInvites(
                page,
                size,
                sortBy,
                currentAuthenticatedUser,
                status
        );
    }
}

