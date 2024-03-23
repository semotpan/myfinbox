package io.myfinbox.expense;

import io.myfinbox.expense.Category.CategoryIdentifier;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.PaymentType;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
class CreateExpenseService implements CreateExpenseUseCase {

    static final String VALIDATION_FAILURE_MESSAGE = "The validation for the create expense request has failed.";
    static final String CATEGORY_NOT_FOUND_MESSAGE = "Category for the provided account was not found.";

    private final ExpenseCommandValidator validator = new ExpenseCommandValidator();

    private final Categories categories;
    private final Expenses expenses;

    @Override
    public Either<Failure, Expense> create(ExpenseCommand command) {
        var validation = validator.validate(command);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(VALIDATION_FAILURE_MESSAGE, validation.getError().toJavaList()));
        }

        var category = categories.findByIdAndAccount(new CategoryIdentifier(command.categoryId()), new AccountIdentifier(command.accountId()));
        if (category.isEmpty()) {
            return Either.left(Failure.ofNotFound(CATEGORY_NOT_FOUND_MESSAGE));
        }

        var expense = Expense.builder()
                .account(new AccountIdentifier(command.accountId()))
                .amount(Money.of(command.amount(), command.currencyCode()))
                .expenseDate(command.expenseDate())
                .paymentType(PaymentType.fromValue(command.paymentType()))
                .description(command.description())
                .category(category.get())
                .build();

        expenses.save(expense);

        return Either.right(expense);
    }
}
