package io.myfinbox.account.application;

import io.myfinbox.account.domain.Account;
import io.myfinbox.account.domain.Accounts;
import io.myfinbox.shared.Failure;
import io.myfinbox.shared.Failure.FieldViolation;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

import static io.myfinbox.account.application.CreateAccountUseCase.CreateAccountCommand.*;
import static io.vavr.API.Invalid;
import static io.vavr.API.Valid;

@Service
@Transactional
@RequiredArgsConstructor
class CreateAccountService implements CreateAccountUseCase {

    static final String ERROR_MESSAGE = "Validation failed for the create account request.";

    private final CommandValidator validator = new CommandValidator();
    private final Accounts accounts;

    @Override
    public Either<Failure, Account> create(CreateAccountCommand cmd) {
        var validation = validator.validate(cmd);
        if (validation.isInvalid()) {
            return Either.left(Failure.ofValidation(ERROR_MESSAGE, validation.getError().toJavaList()));
        }

        if (accounts.existsByEmailAddress(new Account.EmailAddress(cmd.emailAddress()))) {
            return Either.left(Failure.ofConflict("Email address '%s' already exists.".formatted(cmd.emailAddress())));
        }

        var account = Account.builder()
                .firstName(cmd.firstName())
                .lastName(cmd.lastName())
                .emailAddress(new Account.EmailAddress(cmd.emailAddress()))
                .build();

        accounts.save(account);

        return Either.right(account);
    }

    private static final class CommandValidator {
        Validation<Seq<FieldViolation>, CreateAccountCommand> validate(CreateAccountCommand cmd) {
            return Validation.combine(
                    validateFirstName(cmd.firstName()),
                    validateLastName(cmd.lastName()),
                    validateEmailAddress(cmd.emailAddress())
            ).ap((firstName, lastName, emailAddress) -> cmd);
        }

        private Validation<FieldViolation, String> validateFirstName(String firstName) {
            if (StringUtils.isBlank(firstName) || firstName.length() <= Account.MAX_LENGTH) {
                return Valid(firstName);
            }

            return Invalid(FieldViolation.builder()
                    .field(FIELD_FIRST_NAME)
                    .message("First name length cannot exceed '%d' characters.".formatted(Account.MAX_LENGTH))
                    .rejectedValue(firstName)
                    .build());
        }

        private Validation<FieldViolation, String> validateLastName(String lastName) {
            if (StringUtils.isBlank(lastName) || lastName.length() <= Account.MAX_LENGTH) {
                return Valid(lastName);
            }


            return Invalid(FieldViolation.builder()
                    .field(FIELD_LAST_NAME)
                    .message("Last name length cannot exceed '%d' characters.".formatted(Account.MAX_LENGTH))
                    .rejectedValue(lastName)
                    .build());
        }

        private Validation<FieldViolation, String> validateEmailAddress(String emailAddress) {
            if (StringUtils.isBlank(emailAddress)) {
                return Invalid(FieldViolation.builder()
                        .field(FIELD_EMAIL_ADDRESS)
                        .message("Email address cannot be empty.")
                        .rejectedValue(emailAddress)
                        .build());
            }

            if (Account.MAX_LENGTH < emailAddress.length()) {
                return Invalid(FieldViolation.builder()
                        .field(FIELD_EMAIL_ADDRESS)
                        .message("Email address length cannot exceed '%d' characters.".formatted(Account.MAX_LENGTH))
                        .rejectedValue(emailAddress)
                        .build());
            }

            if (!Pattern.compile(Account.patternRFC5322).matcher(emailAddress).matches()) {
                return Invalid(FieldViolation.builder()
                        .field(FIELD_EMAIL_ADDRESS)
                        .message("Email address must follow RFC 5322 standard.")
                        .rejectedValue(emailAddress)
                        .build());
            }

            return Valid(emailAddress);
        }
    }
}
