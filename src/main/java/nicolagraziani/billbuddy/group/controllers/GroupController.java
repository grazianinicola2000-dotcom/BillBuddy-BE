package nicolagraziani.billbuddy.group.controllers;

import nicolagraziani.billbuddy.group.payloads.CreateGroupDTO;
import nicolagraziani.billbuddy.group.payloads.GroupDetailsResponseDTO;
import nicolagraziani.billbuddy.group.payloads.GroupResponseDTO;
import nicolagraziani.billbuddy.group.services.GroupService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponseDTO saveNewGroup(@AuthenticationPrincipal User currentAuthenticatedUser, @Validated @RequestBody CreateGroupDTO body) {
        return this.groupService.saveGroup(body, currentAuthenticatedUser);
    }

    @GetMapping("/me")
    public Page<GroupResponseDTO> findMyGroups(@AuthenticationPrincipal User currentAuthenticatedUser,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               @RequestParam(defaultValue = "joinedAt") String sortBy) {
        return this.groupService.findMyGroups(page, size, sortBy, currentAuthenticatedUser);
    }

    @GetMapping("/{groupId}")
    public GroupDetailsResponseDTO findGroupDetails(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        return this.groupService.findGroupDetails(groupId, currentAuthenticatedUser);
    }
}
