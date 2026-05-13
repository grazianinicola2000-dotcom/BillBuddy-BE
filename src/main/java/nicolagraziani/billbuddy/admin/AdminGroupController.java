package nicolagraziani.billbuddy.admin;

import nicolagraziani.billbuddy.group.payloads.GroupResponseDTO;
import nicolagraziani.billbuddy.group.services.GroupService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/groups")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminGroupController {

    private final GroupService groupService;

    public AdminGroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @GetMapping
    public Page<GroupResponseDTO> findAll(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          @RequestParam(defaultValue = "createdAt") String sortBy) {
        return this.groupService.findAll(page, size, sortBy);
    }
}
