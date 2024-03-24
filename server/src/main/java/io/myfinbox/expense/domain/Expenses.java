package io.myfinbox.expense.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static io.myfinbox.expense.domain.Expense.ExpenseIdentifier;

@Repository
public interface Expenses extends JpaRepository<Expense, ExpenseIdentifier> {

    boolean existsByCategory(Category category);

    Optional<Expense> findByIdAndAccount(ExpenseIdentifier id, AccountIdentifier account);

}
