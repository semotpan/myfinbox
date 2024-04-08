package io.myfinbox.income.application;

import io.myfinbox.income.domain.Income.IncomeIdentifier;
import io.myfinbox.income.domain.Incomes;
import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.util.Objects.isNull;

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

        var income = incomes.findById(new IncomeIdentifier(incomeId));
        if (income.isEmpty()) {
            return Either.left(Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE));
        }

        income.get().delete();

        incomes.delete(income.get());

        return Either.right(null);
    }
}
