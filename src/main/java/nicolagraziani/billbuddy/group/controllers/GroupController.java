package nicolagraziani.billbuddy.group.controllers;

import nicolagraziani.billbuddy.exceptions.ValidationException;
import nicolagraziani.billbuddy.group.payloads.CreateGroupDTO;
import nicolagraziani.billbuddy.group.payloads.GroupDetailsResponseDTO;
import nicolagraziani.billbuddy.group.payloads.GroupResponseDTO;
import nicolagraziani.billbuddy.group.services.GroupService;
import nicolagraziani.billbuddy.user.User;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public GroupResponseDTO saveNewGroup(@AuthenticationPrincipal User currentAuthenticatedUser, @RequestBody @Validated CreateGroupDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }
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

    @PutMapping("/{groupId}")
    public GroupResponseDTO updateGroup(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser, @RequestBody @Validated CreateGroupDTO body, BindingResult validation) {
        if (validation.hasErrors()) {
            List<String> errors = validation.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
            throw new ValidationException(errors);
        }
        return this.groupService.updateGroup(groupId, currentAuthenticatedUser, body);
    }

    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID groupId, @AuthenticationPrincipal User currentAuthenticatedUser) {
        this.groupService.deleteGroup(groupId, currentAuthenticatedUser);
    }
}
