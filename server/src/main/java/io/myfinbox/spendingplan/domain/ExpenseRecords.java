package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.ExpenseRecord.ExpenseIdentifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRecords extends CrudRepository<ExpenseRecord, Long> {

    @Query(value = "SELECT er FROM ExpenseRecord er WHERE er.expenseId = :expenseId")
    List<ExpenseRecord> findByExpenseId(ExpenseIdentifier expenseId);

    @Query(value = "SELECT er FROM ExpenseRecord er WHERE er.expenseId = :expenseId AND er.categoryId = :categoryId")
    List<ExpenseRecord> findByExpenseIdAndCategoryId(ExpenseIdentifier expenseId, CategoryIdentifier categoryId);

}
