package io.myfinbox.income.application;

import io.myfinbox.income.domain.AccountIdentifier;
import io.myfinbox.income.domain.IncomeSource;
import io.myfinbox.income.domain.IncomeSources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class IncomeSourceQueryService implements IncomeSourceQuery {

    private final IncomeSources incomeSources;

    @Override
    public List<IncomeSource> search(UUID accountId) {
        if (isNull(accountId)) {
            return emptyList();
        }

        return incomeSources.findByAccount(new AccountIdentifier(accountId));
    }
}
