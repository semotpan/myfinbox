package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.CreateClassicPlanResource;
import io.myfinbox.rest.PlanResource;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.spendingplan.application.ClassicPlanBuilderUseCase;
import io.myfinbox.spendingplan.application.UpdatePlanUseCase;
import io.myfinbox.spendingplan.application.ClassicPlanBuilderUseCase.CreateClassicPlanCommand;
import io.myfinbox.spendingplan.application.CreatePlanUseCase;
import io.myfinbox.spendingplan.application.PlanCommand;
import io.myfinbox.spendingplan.domain.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/v1/plans")
@RequiredArgsConstructor
final class PlanController implements PlansApi {

    private final CreatePlanUseCase createPlanUseCase;
    private final ClassicPlanBuilderUseCase classicPlanBuilderUseCase;
    private final UpdatePlanUseCase updatePlanUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody PlanResource resource) {
        return createPlanUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> created(fromCurrentRequest().path("/{id}").build(plan.getId().id()))
                        .body(toResource(plan)));
    }

    @PostMapping(path = "/classic", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createClassic(@RequestBody CreateClassicPlanResource resource) {
        return classicPlanBuilderUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> created(fromCurrentRequest().path("/{id}").build(plan.getId().id()))
                        .body(toResource(plan)));
    }

    @PutMapping(path = "/{planId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID planId, @RequestBody PlanResource resource) {
        return updatePlanUseCase.update(planId, toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> ok().body(toResource(plan)));
    }

    private PlanResource toResource(Plan plan) {
        return new PlanResource()
                .planId(plan.getId().id())
                .name(plan.getName())
                .creationTimestamp(plan.getCreationTimestamp().toString())
                .amount(plan.getAmountAsNumber())
                .currencyCode(plan.getCurrencyCode())
                .accountId(plan.getAccount().id())
                .description(plan.getDescription());
    }

    private PlanCommand toCommand(PlanResource resource) {
        return PlanCommand.builder()
                .accountId(resource.getAccountId())
                .name(resource.getName())
                .amount(resource.getAmount())
                .currencyCode(resource.getCurrencyCode())
                .description(resource.getDescription())
                .build();
    }

    private CreateClassicPlanCommand toCommand(CreateClassicPlanResource resource) {
        return CreateClassicPlanCommand.builder()
                .accountId(resource.getAccountId())
                .amount(resource.getAmount())
                .currencyCode(resource.getCurrencyCode())
                .build();
    }
}
