package io.myfinbox.account.adapter.web;

import io.myfinbox.account.application.CreateAccountUseCase;
import io.myfinbox.account.application.CreateAccountUseCase.CreateAccountCommand;
import io.myfinbox.shared.AccountCreateResource;
import io.myfinbox.shared.ApiFailureHandler;
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
@RequestMapping(path = "/accounts")
@RequiredArgsConstructor
final class AccountController implements AccountControllerApi {

    private final CreateAccountUseCase createAccountUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody AccountCreateResource resource) {
        var command = CreateAccountCommand.builder()
                .firstName(resource.getFirstName())
                .lastName(resource.getLastName())
                .emailAddress(resource.getEmailAddress())
                .build();

        return createAccountUseCase.create(command).fold(apiFailureHandler::handle,
                account -> created(fromCurrentRequest().path("/{id}").build(account.getId().toString()))
                        .body(resource.accountId(account.getId().id())));
    }
}
