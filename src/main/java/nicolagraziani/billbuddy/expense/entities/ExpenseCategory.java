package nicolagraziani.billbuddy.expense.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "expense_categories")
@NoArgsConstructor
@Getter
@Setter
public class ExpenseCategory {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE)
    private UUID expenseCategoryId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 500)
    private String icon;

    public ExpenseCategory(String name) {
        this.name = name;
        this.icon = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+");
    }
}
