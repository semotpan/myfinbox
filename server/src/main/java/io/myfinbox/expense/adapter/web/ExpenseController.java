package io.myfinbox.expense.adapter.web;

import io.myfinbox.expense.application.CreateExpenseUseCase;
import io.myfinbox.expense.application.DeleteExpenseUseCase;
import io.myfinbox.expense.application.ExpenseCommand;
import io.myfinbox.expense.application.UpdateExpenseUseCase;
import io.myfinbox.expense.domain.Expense;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.shared.ExpenseResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/expenses")
@RequiredArgsConstructor
final class ExpenseController implements ExpenseControllerApi {

    private final CreateExpenseUseCase createExpenseUseCase;
    private final UpdateExpenseUseCase updateExpenseUseCase;
    private final DeleteExpenseUseCase deleteExpenseUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody ExpenseResource request) {
        return createExpenseUseCase.create(toCommand(request))
                .fold(apiFailureHandler::handle,
                        expense -> created(fromCurrentRequest().path("/{id}").build(expense.getId().id()))
                                .body(toResource(expense)));
    }

    @PutMapping(path = "/{expenseId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID expenseId, @RequestBody ExpenseResource request) {
        return updateExpenseUseCase.update(expenseId, toCommand(request))
                .fold(apiFailureHandler::handle, expense -> ok().body(toResource(expense)));
    }

    @DeleteMapping(path = "/{expenseId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable UUID expenseId) {
        return deleteExpenseUseCase.delete(expenseId)
                .fold(apiFailureHandler::handle, ok -> noContent().build());
    }

    private ExpenseCommand toCommand(ExpenseResource request) {
        return ExpenseCommand.builder()
                .accountId(request.getAccountId())
                .categoryId(request.getCategoryId())
                .paymentType(request.getPaymentType())
                .amount(request.getAmount())
                .currencyCode(request.getCurrencyCode())
                .expenseDate(request.getExpenseDate())
                .description(request.getDescription())
                .build();
    }

    private ExpenseResource toResource(Expense expense) {
        return new ExpenseResource()
                .expenseId(expense.getId().id())
                .creationTimestamp(expense.getCreationTimestamp().toString())
                .accountId(expense.getAccount().id())
                .categoryId(expense.getCategory().getId().id())
                .paymentType(expense.getPaymentType().value())
                .amount(expense.getAmountAsNumber())
                .currencyCode(expense.getCurrencyCode())
                .expenseDate(expense.getExpenseDate())
                .description(expense.getDescription());
    }
}
