package io.myfinbox.income.domain;

import io.myfinbox.income.domain.Income.IncomeIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * The {@link Incomes} interface serves as a repository for managing {@link Income} entities
 */
public interface Incomes extends JpaRepository<Income, IncomeIdentifier> {

    boolean existsByIncomeSource(IncomeSource incomeSource);

}
