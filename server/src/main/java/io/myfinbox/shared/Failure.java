package io.myfinbox.shared;

import lombok.Builder;

import java.util.Collection;

import static io.myfinbox.shared.Guards.notNull;

/**
 * A marker interface representing different types of failures that can occur in the application.
 */
public interface Failure {

    /**
     * Creates a {@link ValidationFailure} instance with the provided message and field violations.
     *
     * @param message         The message describing the validation failure.
     * @param fieldViolations The collection of field violations associated with the validation failure.
     * @return A {@link ValidationFailure} instance.
     */
    static Failure ofValidation(String message, Collection<FieldViolation> fieldViolations) {
        return new ValidationFailure(message, fieldViolations);
    }

    /**
     * Creates a {@link NotFoundFailure} instance with the provided message.
     *
     * @param message The message describing the not found failure.
     * @return A {@link NotFoundFailure} instance.
     */
    static Failure ofNotFound(String message) {
        return new NotFoundFailure(message);
    }

    /**
     * Creates a {@link ConflictFailure} instance with the provided message.
     *
     * @param message The message describing the conflict failure.
     * @return A {@link ConflictFailure} instance.
     */
    static Failure ofConflict(String message) {
        return new ConflictFailure(message);
    }


    /**
     * Creates a {@link ForbiddenFailure} instance with the provided message.
     *
     * @param message The message describing the forbidden failure.
     * @return A {@link ForbiddenFailure} instance.
     */
    static Failure ofForbidden(String message) {
        return new ForbiddenFailure(message);
    }

    record ValidationFailure(String message, Collection<FieldViolation> fieldViolations) implements Failure {

        public ValidationFailure {
            notNull(fieldViolations, "fieldViolations cannot be null");
        }
    }

    record NotFoundFailure(String message) implements Failure {
    }

    record ConflictFailure(String message) implements Failure {
    }

    record ForbiddenFailure(String message) implements Failure {
    }

    @Builder
    record FieldViolation(String field, String message, Object rejectedValue) {
    }

}
