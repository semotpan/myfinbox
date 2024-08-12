package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.CreateClassicPlanResource;
import io.myfinbox.rest.PlanResource;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.application.*;
import io.myfinbox.spendingplan.application.ClassicPlanBuilderUseCase.CreateClassicPlanCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
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
    private final PlanQuery planQuery;
    private final ApiFailureHandler apiFailureHandler;
    private final ConversionService conversionService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody PlanResource resource) {
        return createPlanUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> created(fromCurrentRequest().path("/{id}").build(plan.getId().id()))
                        .body(conversionService.convert(plan, PlanResource.class)));
    }

    @PostMapping(path = "/classic", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createClassic(@RequestBody CreateClassicPlanResource resource) {
        return classicPlanBuilderUseCase.create(toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> created(fromCurrentRequest().path("/{id}").build(plan.getId().id()))
                        .body(conversionService.convert(plan, PlanResource.class)));
    }

    @PutMapping(path = "/{planId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID planId, @RequestBody PlanResource resource) {
        return updatePlanUseCase.update(planId, toCommand(resource))
                .fold(apiFailureHandler::handle, plan -> ok().body(conversionService.convert(plan, PlanResource.class)));
    }

    @GetMapping(path = "/{planId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> one(@PathVariable UUID planId) {
        var plans = planQuery.search()
                .withPlanId(planId)
                .list();

        if (plans.isEmpty()) {
            return apiFailureHandler.handle(Failure.ofNotFound("Plan with ID '%s' was not found.".formatted(planId)));
        }

        return ok().body(conversionService.convert(plans.getFirst(), PlanResource.class));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list(@RequestParam("accountId") UUID accountId) {
        var plans = planQuery.search()
                .withAccountId(accountId)
                .list();

        return ok().body(plans.stream()
                .map(plan -> conversionService.convert(plan, PlanResource.class))
                .toList());
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
