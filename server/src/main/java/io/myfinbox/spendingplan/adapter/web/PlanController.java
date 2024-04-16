package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.CreatePlanResource;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.spendingplan.application.CreatePlanUseCase;
import io.myfinbox.spendingplan.application.PlanCommand;
import io.myfinbox.spendingplan.domain.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/v1/plans")
@RequiredArgsConstructor
final class PlanController implements PlansApi {

    private final CreatePlanUseCase createPlanUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody CreatePlanResource resource) {
        return createPlanUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> created(fromCurrentRequest().path("/{id}").build(plan.getId().id()))
                        .body(toCreatedResource(plan)));
    }

    private CreatePlanResource toCreatedResource(Plan plan) {
        return new CreatePlanResource()
                .planId(plan.getId().id())
                .name(plan.getName())
                .creationTimestamp(plan.getCreationTimestamp().toString())
                .amount(plan.getAmountAsNumber())
                .currencyCode(plan.getCurrencyCode())
                .accountId(plan.getAccount().id())
                .description(plan.getDescription());
    }

    private PlanCommand toCommand(CreatePlanResource resource) {
        return PlanCommand.builder()
                .accountId(resource.getAccountId())
                .name(resource.getName())
                .amount(resource.getAmount())
                .currencyCode(resource.getCurrencyCode())
                .description(resource.getDescription())
                .build();
    }
}
