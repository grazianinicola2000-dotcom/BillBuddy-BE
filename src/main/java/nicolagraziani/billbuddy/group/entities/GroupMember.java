package nicolagraziani.billbuddy.group.entities;

import jakarta.persistence.*;
import lombok.*;
import nicolagraziani.billbuddy.group.enums.GroupRole;
import nicolagraziani.billbuddy.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class GroupMember {
    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID groupMemberId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false, name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    @Column(nullable = false, name = "joined_at")
    private LocalDateTime joinedAt;

    public GroupMember(User userId, Group groupId, GroupRole role) {
        this.user = userId;
        this.group = groupId;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }
}
