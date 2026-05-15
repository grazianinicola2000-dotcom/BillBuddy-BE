package nicolagraziani.billbuddy.expense.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nicolagraziani.billbuddy.expense.enums.CurrencyCode;
import nicolagraziani.billbuddy.expense.enums.ExpenseType;
import nicolagraziani.billbuddy.group.entities.Group;
import nicolagraziani.billbuddy.user.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expenses")
@NoArgsConstructor
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID expenseId;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(nullable = false, name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "expense_type")
    private ExpenseType expenseType;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne
    @JoinColumn(nullable = false, name = "paid_by")
    private User paidBy;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private ExpenseCategory expenseCategory;

    @Column(nullable = false, name = "expense_date")
    private LocalDate expenseDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyCode;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSplit> splits = new ArrayList<>();

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Expense(String title, String description, BigDecimal totalAmount, ExpenseType expenseType, Group group, User paidBy, ExpenseCategory expenseCategory, LocalDate expenseDate, CurrencyCode currencyCode) {
        this.title = title;
        this.description = description;
        this.totalAmount = totalAmount;
        this.expenseType = expenseType;
        this.group = group;
        this.paidBy = paidBy;
        this.expenseCategory = expenseCategory;
        this.expenseDate = expenseDate;
        this.currencyCode = currencyCode;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
    }

    public void addSplit(ExpenseSplit split) {
        splits.add(split);
        split.setExpense(this);
    }

    public void removeSplit(ExpenseSplit split) {
        splits.remove(split);
        split.setExpense(null);
    }
}
