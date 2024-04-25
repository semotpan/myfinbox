package io.myfinbox.income.application;

import io.myfinbox.income.domain.Income.IncomeIdentifier;
import io.myfinbox.income.domain.Incomes;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
class DeleteIncomeService implements DeleteIncomeUseCase {

    static final String INCOME_NOT_FOUND_MESSAGE = "Income was not found.";

    private final Incomes incomes;

    @Override
    public Either<Failure, Void> delete(UUID incomeId) {
        if (isNull(incomeId)) {
            return Either.left(Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE));
        }

        var possibleIncome = incomes.findById(new IncomeIdentifier(incomeId));
        if (possibleIncome.isEmpty()) {
            return Either.left(Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE));
        }

        possibleIncome.get().delete();

        incomes.delete(possibleIncome.get());
        log.debug("Income {} was deleted", possibleIncome.get().getId());

        return Either.right(null);
    }
}
