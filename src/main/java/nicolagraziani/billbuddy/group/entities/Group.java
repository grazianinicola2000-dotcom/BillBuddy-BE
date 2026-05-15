package nicolagraziani.billbuddy.group.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nicolagraziani.billbuddy.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups")
@NoArgsConstructor
@Getter
@Setter
public class Group {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID groupId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne
    @JoinColumn(nullable = false, name = "created_by")
    private User createdBy;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Group(String name, String description, User createdBy) {
        this.name = name;
        this.description = description;
        this.imageUrl = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+");
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }
}
