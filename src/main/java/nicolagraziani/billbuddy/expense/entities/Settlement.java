package nicolagraziani.billbuddy.expense.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@NoArgsConstructor
@Getter
@Setter
public class Settlement {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID settlementId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "payer_id")
    private User payer;

    @ManyToOne
    @JoinColumn(nullable = false, name = "receiver_id")
    private User receiver;

    @Column(nullable = false, name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    @Column(nullable = false, name = "settlement_date")
    private LocalDateTime settlementDate;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private String note;

    public Settlement(User payer, User receiver, BigDecimal amount, CurrencyCode currencyCode, LocalDateTime settlementDate, Group group, String note) {
        this.payer = payer;
        this.receiver = receiver;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.settlementDate = settlementDate;
        this.group = group;
        this.createdAt = LocalDateTime.now();
        this.note = note;
    }
}
