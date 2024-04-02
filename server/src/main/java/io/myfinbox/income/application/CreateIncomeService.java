package io.myfinbox.income.application;

import io.myfinbox.income.domain.AccountIdentifier;
import io.myfinbox.income.domain.Income;
import io.myfinbox.income.domain.IncomeSource.IncomeSourceIdentifier;
import io.myfinbox.income.domain.IncomeSources;
import io.myfinbox.income.domain.Incomes;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.PaymentType;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
class CreateIncomeService implements CreateIncomeUseCase {

    static final String VALIDATION_FAILURE_MESSAGE = "Validation failed for the create income request.";
    static final String INCOME_SOURCE_NOT_FOUND_MESSAGE = "Income source not found for the provided account.";

    private final IncomeCommandValidator validator = new IncomeCommandValidator();

    private final IncomeSources incomeSources;
    private final Incomes incomes;

    @Override
    public Either<Failure, Income> create(IncomeCommand command) {
        var validation = validator.validate(command);

        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        var incomeSource = incomeSources.findByIdAndAccount(new IncomeSourceIdentifier(command.incomeSourceId()), new AccountIdentifier(command.accountId()));

        if (incomeSource.isEmpty()) {
            return Either.left(Failure.ofNotFound(INCOME_SOURCE_NOT_FOUND_MESSAGE));
        }

        var income = Income.builder()
                .account(new AccountIdentifier(command.accountId()))
                .amount(Money.of(command.amount(), command.currencyCode()))
                .paymentType(PaymentType.fromValue(command.paymentType()))
                .incomeDate(command.incomeDate())
                .description(command.description())
                .incomeSource(incomeSource.get())
                .build();

        incomes.save(income);

        return Either.right(income);
    }
}
