package nicolagraziani.billbuddy.group.entities;

import jakarta.persistence.*;
import lombok.*;
import nicolagraziani.billbuddy.group.enums.InviteStatus;
import nicolagraziani.billbuddy.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invites")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Invite {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID inviteId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(nullable = false, name = "invited_by")
    private User invitedBy;

    @ManyToOne
    @JoinColumn(nullable = false, name = "receiver_id")
    private User receiver;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status;

    public Invite(Group group, User invitedBy, User receiver) {
        this.group = group;
        this.invitedBy = invitedBy;
        this.receiver = receiver;
        this.expiresAt = LocalDateTime.now().plusDays(1);
        this.status = InviteStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }
}
