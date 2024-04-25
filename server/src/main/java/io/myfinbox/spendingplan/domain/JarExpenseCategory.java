package io.myfinbox.spendingplan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.myfinbox.shared.Guards.notNull;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PACKAGE;

@Entity
@Getter
@ToString
@Table(name = "spending_jar_expense_category")
@EqualsAndHashCode(of = {"jar", "categoryId"})
@NoArgsConstructor(access = PACKAGE, force = true)
public class JarExpenseCategory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "category_id"))
    private final CategoryIdentifier categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jar_id", referencedColumnName = "id", nullable = false)
    private final Jar jar;

    @OneToMany(mappedBy = "jarExpenseCategory", cascade = ALL, orphanRemoval = true)
    private List<ExpenseRecord> expenseRecords = new ArrayList<>();

    @Builder
    public JarExpenseCategory(Jar jar, CategoryIdentifier categoryId) {
        this.jar = notNull(jar, "jar cannot be null.");
        this.categoryId = notNull(categoryId, "categoryId cannot be null.");
        this.creationTimestamp = Instant.now();
    }
}
