package nicolagraziani.billbuddy.group.controllers;

import nicolagraziani.billbuddy.group.services.GroupMemberService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    public GroupMemberController(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable UUID groupId, @PathVariable UUID userId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.groupMemberService.removeMember(groupId, userId, currentAuthenticatedUser);
    }

    @PatchMapping("/{groupId}/members/{userId}/promote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void promoteToAdmin(@PathVariable UUID groupId, @PathVariable UUID userId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.groupMemberService.promoteToAdmin(groupId, userId, currentAuthenticatedUser);
    }

    @PatchMapping("/{groupId}/members/{userId}/demote")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void demoteToMember(@PathVariable UUID groupId, @PathVariable UUID userId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.groupMemberService.demoteToMember(groupId, userId, currentAuthenticatedUser);
    }
}
