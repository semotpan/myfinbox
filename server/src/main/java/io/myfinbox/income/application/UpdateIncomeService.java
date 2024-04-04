package io.myfinbox.income.application;

import io.myfinbox.income.domain.*;
import io.myfinbox.income.domain.Income.IncomeIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.PaymentType;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional
class UpdateIncomeService implements UpdateIncomeUseCase {

    static final String VALIDATION_FAILURE_MESSAGE = "Validation failed for the update income request.";
    static final String INCOME_SOURCE_NOT_FOUND_MESSAGE = "Income source not found for the provided account.";
    static final String INCOME_NOT_FOUND_MESSAGE = "Income not found.";

    private final IncomeCommandValidator validator = new IncomeCommandValidator();

    private final IncomeSources incomeSources;
    private final Incomes incomes;

    @Override
    public Either<Failure, Income> update(UUID incomeId, IncomeCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        if (isNull(incomeId)) {
            return Either.left(Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE));
        }

        var income = incomes.findByIdAndAccount(new IncomeIdentifier(incomeId), new AccountIdentifier(command.accountId()));
        if (income.isEmpty()) {
            return Either.left(Failure.ofNotFound(INCOME_NOT_FOUND_MESSAGE));
        }

        var possibleIncomeSource = fetchIncomeSourceOrFailure(income.get().getIncomeSource(), command.incomeSourceId(), command.accountId());
        if (possibleIncomeSource.isLeft()) {
            return Either.left(possibleIncomeSource.getLeft());
        }

        income.get().update(
                Income.builder()
                        .account(new AccountIdentifier(command.accountId()))
                        .amount(Money.of(command.amount(), command.currencyCode()))
                        .paymentType(PaymentType.fromValue(command.paymentType()))
                        .incomeDate(command.incomeDate())
                        .description(command.description())
                        .incomeSource(possibleIncomeSource.get())
        );

        incomes.save(income.get());

        return Either.right(income.get());
    }

    private Either<Failure, IncomeSource> fetchIncomeSourceOrFailure(IncomeSource incomeSource, UUID incomeSourceId, UUID accountId) {
        if (!incomeSource.matches(new IncomeSourceIdentifier(incomeSourceId))) {
            var possibleIncome = incomeSources.findByIdAndAccount(new IncomeSourceIdentifier(incomeSourceId), new AccountIdentifier(accountId));
            if (possibleIncome.isEmpty()) {
                return Either.left(Failure.ofNotFound(INCOME_SOURCE_NOT_FOUND_MESSAGE));
            }
            incomeSource = possibleIncome.get();
        }

        return Either.right(incomeSource);
    }
}
