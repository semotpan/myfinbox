package io.myfinbox.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static io.myfinbox.expense.Expense.ExpenseIdentifier;

@Repository
public interface Expenses extends JpaRepository<Expense, ExpenseIdentifier> {
}
