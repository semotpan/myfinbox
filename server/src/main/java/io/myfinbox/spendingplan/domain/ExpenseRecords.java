package io.myfinbox.spendingplan.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRecords extends CrudRepository<ExpenseRecord, Long> {
}
