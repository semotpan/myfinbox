package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.AccountIdentifier;
import io.myfinbox.expense.domain.Categories;
import io.myfinbox.expense.domain.Category.CategoryIdentifier;
import io.myfinbox.expense.domain.Expense;
import io.myfinbox.expense.domain.Expenses;
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

    static final String VALIDATION_FAILURE_MESSAGE = "Validation failed for the create expense request.";
    static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found for the provided account.";

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
