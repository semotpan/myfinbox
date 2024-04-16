package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.CreateJarResource;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.spendingplan.application.CreateJarUseCase;
import io.myfinbox.spendingplan.application.JarCommand;
import io.myfinbox.spendingplan.domain.Jar;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/plans")
final class JarController implements JarsApi {

    private final CreateJarUseCase createJarUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(path = "/{planId}/jars", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@PathVariable UUID planId, @RequestBody CreateJarResource resource) {
        return createJarUseCase.create(planId, toCommand(resource))
                .fold(apiFailureHandler::handle, jar -> created(fromCurrentRequest().path("/{id}").build(jar.getId().id()))
                        .body(toResource(jar)));
    }

    private JarCommand toCommand(CreateJarResource resource) {
        return JarCommand.builder()
                .name(resource.getName())
                .percentage(resource.getPercentage())
                .description(resource.getDescription())
                .build();
    }

    private CreateJarResource toResource(Jar jar) {
        return new CreateJarResource()
                .jarId(jar.getId().id())
                .creationTimestamp(jar.getCreationTimestamp().toString())
                .amountToReach(jar.getAmountToReachAsNumber())
                .currencyCode(jar.getCurrencyCode())
                .name(jar.getName())
                .percentage(jar.getPercentage().value())
                .description(jar.getDescription());
    }
}
