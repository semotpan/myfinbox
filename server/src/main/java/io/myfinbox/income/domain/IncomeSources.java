package io.myfinbox.income.domain;

import io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing income sources.
 */
@Repository
public interface IncomeSources extends JpaRepository<IncomeSource, IncomeSourceIdentifier> {

    List<IncomeSource> findByAccount(AccountIdentifier account);

}

