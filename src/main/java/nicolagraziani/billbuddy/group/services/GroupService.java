package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.group.payloads.CreateGroupDTO;
import nicolagraziani.billbuddy.group.payloads.GroupResponseDTO;
import nicolagraziani.billbuddy.group.repositories.GroupRepository;
import nicolagraziani.billbuddy.user.User;
import nicolagraziani.billbuddy.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberService groupMemberService;
    private final UserService userService;

    public GroupService(GroupRepository groupRepository, GroupMemberService groupMemberService, UserService userService) {
        this.groupRepository = groupRepository;
        this.groupMemberService = groupMemberService;
        this.userService = userService;
    }

    public Group findGroupById(UUID groupId) {
        return this.groupRepository.findById(groupId).orElseThrow(() -> new NotFoundException(groupId));
    }

    @Transactional
    public GroupResponseDTO saveGroup(CreateGroupDTO body, User creator) {
        User foundUser = this.userService.findUserById(creator.getUserId());
        Group newGroup = new Group(body.name(), body.description(), foundUser);
        this.groupRepository.save(newGroup);
        Group foundGroup = findGroupById(newGroup.getGroupId());
        this.groupMemberService.saveGroupMember(foundUser, foundGroup, GroupRole.OWNER);
        return new GroupResponseDTO(foundGroup.getGroupId(), foundGroup.getName(), foundGroup.getDescription(), foundGroup.getImageUrl(), foundGroup.getCreatedAt());
    }
}
