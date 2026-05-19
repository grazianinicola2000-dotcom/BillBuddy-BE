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
    @JoinColumn(nullable = false, name = "debtor_id")
    private User debtor;

    @ManyToOne
    @JoinColumn(nullable = false, name = "creditor_id")
    private User creditor;

    @Column(nullable = false, name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    @ManyToOne
    @JoinColumn(nullable = false, name = "group_id")
    private Group group;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(nullable = false, name = "expense_split_id")
    private ExpenseSplit expenseSplit;

    @Column
    private String note;

    public Settlement(User payer, User receiver, BigDecimal amount, CurrencyCode currencyCode, Group group, String note, ExpenseSplit expenseSplit) {
        this.debtor = payer;
        this.creditor = receiver;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.group = group;
        this.createdAt = LocalDateTime.now();
        this.note = note;
        this.expenseSplit = expenseSplit;
    }
}
