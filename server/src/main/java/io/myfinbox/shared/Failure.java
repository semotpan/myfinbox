package io.myfinbox.shared;

import lombok.Builder;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

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

    record ValidationFailure(String message, Collection<FieldViolation> fieldViolations) implements Failure {

        public ValidationFailure {
            requireNonNull(fieldViolations, "fieldViolations cannot be null");
        }
    }

    record NotFoundFailure(String message) implements Failure {}

    record ConflictFailure(String message) implements Failure {}

    @Builder
    record FieldViolation(String field, String message, Object rejectedValue) {}

}
