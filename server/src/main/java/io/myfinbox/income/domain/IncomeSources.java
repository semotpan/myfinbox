package io.myfinbox.income.domain;

import io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing income sources.
 */
@Repository
public interface IncomeSources extends JpaRepository<IncomeSource, IncomeSourceIdentifier> {

    List<IncomeSource> findByAccount(AccountIdentifier account);

    Optional<IncomeSource> findByIdAndAccount(IncomeSourceIdentifier incomeSourceId, AccountIdentifier accountId);

    boolean existsByNameAndAccount(String name, AccountIdentifier accountId);

}

