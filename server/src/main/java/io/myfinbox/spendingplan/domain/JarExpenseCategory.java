package io.myfinbox.spendingplan.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import static io.myfinbox.shared.Guards.notNull;
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

    @Builder
    public JarExpenseCategory(Jar jar, CategoryIdentifier categoryId) {
        this.jar = notNull(jar, "jar cannot be null.");
        this.categoryId = notNull(categoryId, "categoryId cannot be null.");
        this.creationTimestamp = Instant.now();
    }

    @Embeddable
    public record CategoryIdentifier(UUID id) implements Serializable {

        public CategoryIdentifier {
            notNull(id, "id cannot be null");
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
