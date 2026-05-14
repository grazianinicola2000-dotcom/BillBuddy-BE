package nicolagraziani.billbuddy.group.services;

import lombok.extern.slf4j.Slf4j;
import nicolagraziani.billbuddy.exceptions.NotFoundException;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.group.entities.GroupMember;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.group.payloads.CreateGroupDTO;
import nicolagraziani.billbuddy.group.payloads.GroupDetailsResponseDTO;
import nicolagraziani.billbuddy.group.payloads.GroupMemberResponseDTO;
import nicolagraziani.billbuddy.group.payloads.GroupResponseDTO;
import nicolagraziani.billbuddy.group.repositories.GroupRepository;
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

import java.util.List;
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
        User foundUser = this.userService.findActiveUserById(creator.getUserId());
        Group newGroup = new Group(body.name(), body.description(), foundUser);
        Group savedGroup = this.groupRepository.save(newGroup);
        this.groupMemberService.saveGroupMember(foundUser, savedGroup, GroupRole.OWNER);
        log.info("Group '{}' created by user {}",
                savedGroup.getName(),
                foundUser.getUsername());
        return new GroupResponseDTO(savedGroup.getGroupId(), savedGroup.getName(), savedGroup.getDescription(), savedGroup.getImageUrl(), savedGroup.getCreatedAt());
    }

    public Page<GroupResponseDTO> findAll(int page, int size, String sortBy) {
        if (size > 100 || size < 0) size = 20;
        if (page < 0) page = 0;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.groupRepository.findAll(pageable)
                .map(group -> new GroupResponseDTO(
                        group.getGroupId(), group.getName(), group.getDescription(), group.getImageUrl(), group.getCreatedAt()
                ));
    }

    public Page<GroupResponseDTO> findMyGroups(int page, int size, String sortBy, User user) {
        if (size > 100 || size < 0) size = 20;
        if (page < 0) page = 0;
        User foundUser = this.userService.findActiveUserById(user.getUserId());
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return this.groupMemberService.findByUser(foundUser, pageable)
                .map(groupMember -> {
                    Group group = groupMember.getGroup();
                    return new GroupResponseDTO(
                            group.getGroupId(),
                            group.getName(),
                            group.getDescription(),
                            group.getImageUrl(),
                            group.getCreatedAt()
                    );
                });
    }

    public GroupDetailsResponseDTO findGroupDetails(UUID groupId, User user) {
        User foundUser = this.userService.findActiveUserById(user.getUserId());
        Group foundGroup = this.findGroupById(groupId);

        if (!this.groupMemberService.existsByGroupAndUser(foundGroup, foundUser)) {
            throw new AuthorizationDeniedException("You are not a member of this group");
        }

        List<GroupMember> members = this.groupMemberService.findAllByGroup(foundGroup);

        List<GroupMemberResponseDTO> membersDTO = members.stream().map(
                member -> new GroupMemberResponseDTO(
                        member.getGroupMemberId(),
                        member.getUser().getUserId(),
                        member.getUser().getUsername(),
                        member.getUser().getAvatarURL(),
                        member.getRole(),
                        member.getJoinedAt()
                )
        ).toList();

        return new GroupDetailsResponseDTO(
                foundGroup.getGroupId(),
                foundGroup.getName(),
                foundGroup.getDescription(),
                foundGroup.getImageUrl(),
                foundGroup.getCreatedAt(),
                members.size(),
                membersDTO
        );
    }

    public boolean isOwnerOrSystemAdmin(Group group, User user) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        GroupMember membership = this.groupMemberService.findByGroupAndUser(group, user);
        return membership.getRole() == GroupRole.OWNER;
    }

    @Transactional
    public GroupResponseDTO updateGroup(UUID groupId, User user, CreateGroupDTO body) {
        Group found = this.findGroupById(groupId);
        if (!isOwnerOrSystemAdmin(found, user)) {
            throw new AuthorizationDeniedException("You are not allowed to modify this group");
        }
        found.setName(body.name());
        found.setDescription(body.description());
        Group saved = this.groupRepository.save(found);
        log.info("Group '{}' updated by user {}",
                saved.getName(),
                user.getUsername());
        return new GroupResponseDTO(saved.getGroupId(), saved.getName(), saved.getDescription(), saved.getImageUrl(), saved.getCreatedAt());
    }

    @Transactional
    public void deleteGroup(UUID groupId, User user) {
        Group found = this.findGroupById(groupId);
        if (!isOwnerOrSystemAdmin(found, user)) {
            throw new AuthorizationDeniedException("You are not allowed to delete this group");
        }
        this.groupMemberService.deleteAllByGroup(found);
        this.groupRepository.delete(found);
        log.info("Group '{}' deleted by user {}",
                found.getName(),
                user.getUsername());
    }
}
