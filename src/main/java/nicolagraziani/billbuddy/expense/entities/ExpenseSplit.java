package nicolagraziani.billbuddy.expense.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nicolagraziani.billbuddy.user.User;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "expense_splits")
@NoArgsConstructor
@Getter
@Setter
public class ExpenseSplit {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID expenseSplitId;

    @ManyToOne
    @JoinColumn(nullable = false, name = "expense_id")
    private Expense expense;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Column(nullable = false, name = "amount_owed", precision = 10, scale = 2)
    private BigDecimal amountOwed;

    public ExpenseSplit(Expense expense, User user, BigDecimal amountOwed) {
        this.expense = expense;
        this.user = user;
        this.amountOwed = amountOwed;
    }
}
