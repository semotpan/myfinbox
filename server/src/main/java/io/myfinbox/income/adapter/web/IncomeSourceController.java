package io.myfinbox.income.adapter.web;

import io.myfinbox.income.application.IncomeSourceService;
import io.myfinbox.income.domain.IncomeSource;
import io.myfinbox.rest.IncomeSourceResource;
import io.myfinbox.shared.ApiFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static io.myfinbox.income.application.IncomeSourceService.IncomeSourceCommand;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.*;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/incomes/income-source")
@RequiredArgsConstructor
final class IncomeSourceController implements IncomeSourceControllerApi {

    private final IncomeSourceService incomeSourceService;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody IncomeSourceResource resource) {
        return incomeSourceService.create(new IncomeSourceCommand(resource.getName(), resource.getAccountId()))
                .fold(apiFailureHandler::handle,
                        incomeSource -> created(fromCurrentRequest().path("/{id}").build(incomeSource.getId().id()))
                                .body(toResource(incomeSource)));
    }

    @PutMapping(path = "/{incomeSourceId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID incomeSourceId, @RequestBody IncomeSourceResource resource) {
        return incomeSourceService.update(incomeSourceId, new IncomeSourceCommand(resource.getName(), resource.getAccountId()))
                .fold(apiFailureHandler::handle, incomeSource -> ok(toResource(incomeSource)));
    }

    @DeleteMapping(path = "/{incomeSourceId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> delete(@PathVariable UUID incomeSourceId) {
        return incomeSourceService.delete(incomeSourceId)
                .fold(apiFailureHandler::handle, ok -> noContent().build());
    }

    private IncomeSourceResource toResource(IncomeSource incomeSource) {
        return new IncomeSourceResource()
                .incomeSourceId(incomeSource.getId().id())
                .accountId(incomeSource.getAccount().id())
                .name(incomeSource.getName())
                .creationTimestamp(incomeSource.getCreationTimestamp().toString());
    }
}
