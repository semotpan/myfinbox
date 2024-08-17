package io.myfinbox.spendingplan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.myfinbox.shared.Guards.notNull;
import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.GenerationType.SEQUENCE;
import static lombok.AccessLevel.PACKAGE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Entity
@Getter
@ToString(exclude = {"jar", "expenseRecords"})
@Table(name = "spending_jar_expense_category")
@EqualsAndHashCode(of = {"id", "jar", "categoryId"})
@NoArgsConstructor(access = PACKAGE, force = true)
public class JarExpenseCategory {

    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "sjec_seq_id")
    @SequenceGenerator(name = "sjec_seq_id", sequenceName = "sjec_seq_id", allocationSize = 1)
    // https://vladmihalcea.com/migrate-hilo-hibernate-pooled/
    private Long id;

    private final Instant creationTimestamp;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "category_id"))
    private final CategoryIdentifier categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jar_id", referencedColumnName = "id", nullable = false)
    private final Jar jar;

    @OneToMany(mappedBy = "jarExpenseCategory", cascade = ALL, orphanRemoval = true)
    private List<ExpenseRecord> expenseRecords = new ArrayList<>();

    @Builder
    public JarExpenseCategory(Jar jar, CategoryIdentifier categoryId, String categoryName) {
        this.jar = notNull(jar, "jar cannot be null.");
        this.categoryId = notNull(categoryId, "categoryId cannot be null.");
        this.categoryName = !isBlank(categoryName) ? categoryName.trim() : null;
        this.creationTimestamp = Instant.now();
    }
}
