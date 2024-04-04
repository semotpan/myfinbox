package io.myfinbox.income.adapter.web;

import io.myfinbox.income.application.CreateIncomeUseCase;
import io.myfinbox.income.application.IncomeCommand;
import io.myfinbox.income.application.UpdateIncomeUseCase;
import io.myfinbox.income.domain.Income;
import io.myfinbox.rest.IncomeResource;
import io.myfinbox.shared.ApiFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/v1/incomes")
@RequiredArgsConstructor
final class IncomeController implements IncomesApi {

    private final CreateIncomeUseCase createIncomeUseCase;
    private final UpdateIncomeUseCase updateIncomeUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody IncomeResource resource) {
        return createIncomeUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle,
                        income -> created(fromCurrentRequest().path("/{id}").build(income.getId().id()))
                                .body(toResource(income)));
    }

    @PutMapping(path = "/{incomeId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID incomeId, @RequestBody IncomeResource resource) {
        return updateIncomeUseCase.update(incomeId, toCommand(resource))
                .fold(apiFailureHandler::handle, income -> ok().body(toResource(income)));
    }

    private IncomeResource toResource(Income income) {
        return new IncomeResource()
                .incomeId(income.getId().id())
                .accountId(income.getAccount().id())
                .incomeSourceId(income.getIncomeSource().getId().id())
                .amount(income.getAmountAsNumber())
                .currencyCode(income.getCurrencyCode())
                .incomeDate(income.getIncomeDate())
                .creationTimestamp(income.getCreationTimestamp().toString())
                .paymentType(income.getPaymentType().value())
                .description(income.getDescription());
    }

    private IncomeCommand toCommand(IncomeResource resource) {
        return IncomeCommand.builder()
                .accountId(resource.getAccountId())
                .incomeSourceId(resource.getIncomeSourceId())
                .amount(resource.getAmount())
                .currencyCode(resource.getCurrencyCode())
                .paymentType(resource.getPaymentType())
                .incomeDate(resource.getIncomeDate())
                .description(resource.getDescription())
                .build();
    }
}
