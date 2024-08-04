package io.myfinbox.account.adapter.web;

import io.myfinbox.account.application.CreateAccountUseCase;
import io.myfinbox.account.application.CreateAccountUseCase.CreateAccountCommand;
import io.myfinbox.rest.AccountCreateResource;
import io.myfinbox.shared.ApiFailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.Locale;

import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/v1/accounts")
@RequiredArgsConstructor
final class AccountController implements AccountsApi {

    static final Locale defaultLocale = Locale.of("en", "MD");

    private final CreateAccountUseCase createAccountUseCase;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestHeader(name = ACCEPT_LANGUAGE, required = false) Locale locale,
                                    @RequestBody AccountCreateResource resource) {
        var command = CreateAccountCommand.builder()
                .firstName(resource.getFirstName())
                .lastName(resource.getLastName())
                .emailAddress(resource.getEmailAddress())
                .currency(Currency.getInstance(resolve(locale)).getCurrencyCode())
                .zoneId(resource.getZoneId())
                .build();

        return createAccountUseCase.create(command).fold(apiFailureHandler::handle,
                account -> created(fromCurrentRequest().path("/{id}").build(account.getId().toString()))
                        .body(resource.accountId(account.getId().id())
                                .currency(command.currency())
                                .zoneId(resource.getZoneId())
                        ));
    }

    private Locale resolve(Locale locale) {
        try {
            if (isNull(Currency.getInstance(locale).getCurrencyCode())) {
                return defaultLocale;
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            return defaultLocale;
        }

        return locale;
    }
}
