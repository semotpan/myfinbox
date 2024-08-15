package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.JarCategoryModificationResource;
import io.myfinbox.rest.JarResource;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.shared.Failure;
import io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase;
import io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase.JarCategoryToAddOrRemove;
import io.myfinbox.spendingplan.application.CreateJarUseCase;
import io.myfinbox.spendingplan.application.JarCommand;
import io.myfinbox.spendingplan.application.JarQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

import static io.myfinbox.spendingplan.application.AddOrRemoveJarCategoryUseCase.JarCategoriesCommand;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/plans")
final class JarController implements JarsApi {

    private final CreateJarUseCase createJarUseCase;
    private final AddOrRemoveJarCategoryUseCase addOrRemoveJarCategoryUseCase;
    private final JarQuery jarQuery;
    private final ApiFailureHandler apiFailureHandler;
    private final ConversionService conversionService;

    @PostMapping(path = "/{planId}/jars", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@PathVariable UUID planId, @RequestBody JarResource resource) {
        return createJarUseCase.create(planId, toCommand(resource))
                .fold(apiFailureHandler::handle, jar -> created(fromCurrentRequest().path("/{id}").build(jar.getId().id()))
                        .body(conversionService.convert(jar, JarResource.class)));
    }

    @PutMapping(path = "/{planId}/jars/{jarId}/expense-categories", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> modifyExpenseCategories(@PathVariable UUID planId,
                                                     @PathVariable UUID jarId,
                                                     @RequestBody JarCategoryModificationResource resource) {
        return addOrRemoveJarCategoryUseCase.addOrRemove(planId, jarId, toCommand(resource))
                .fold(apiFailureHandler::handle, ok -> ok().build());
    }

    @GetMapping(path = "/{planId}/jars/{jarId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> one(@PathVariable UUID planId, @PathVariable UUID jarId) {
        var jars = jarQuery.search()
                .withPlanId(planId)
                .withJarId(jarId)
                .list();

        if (jars.isEmpty()) {
            return apiFailureHandler.handle(Failure.ofNotFound("Jar with ID '%s' for plan ID '%s' was not found.".formatted(jarId, planId)));
        }

        return ok().body(conversionService.convert(jars.getFirst(), JarResource.class));
    }

    @GetMapping(path = "/{planId}/jars", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> list(@PathVariable("planId") UUID planId) {
        var jars = jarQuery.search()
                .withPlanId(planId)
                .list();

        return ok().body(jars.stream()
                .map(jar -> conversionService.convert(jar, JarResource.class))
                .toList());
    }

    private JarCommand toCommand(JarResource resource) {
        return JarCommand.builder()
                .name(resource.getName())
                .percentage(resource.getPercentage())
                .description(resource.getDescription())
                .build();
    }

    private JarCategoriesCommand toCommand(JarCategoryModificationResource resource) {
        var categoryToAdds = resource.getCategories()
                .stream()
                .filter(Objects::nonNull) // avoid null categoryToAdd
                .map(categoryToAdd -> new JarCategoryToAddOrRemove(categoryToAdd.getCategoryId(), categoryToAdd.getCategoryName(), categoryToAdd.getToAdd()))
                .toList();
        return new JarCategoriesCommand(categoryToAdds);
    }
}
