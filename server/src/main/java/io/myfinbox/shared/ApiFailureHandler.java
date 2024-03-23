package io.myfinbox.shared;

import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;

import static io.myfinbox.shared.ApiErrorResponse.*;
import static io.myfinbox.shared.Failure.*;
import static io.myfinbox.shared.Guards.notNull;

/**
 * {@link ApiFailureHandler} defines the mapping between application Failure and REST ApiError Response.
 */
public final class ApiFailureHandler {

    /**
     * Handles the provided failure by mapping it to an appropriate {@link ResponseEntity<ApiErrorResponse>}.
     *
     * @param failure The Failure instance to handle.
     * @return {@link ResponseEntity<ApiErrorResponse>} representing the appropriate error response.
     * @throws IllegalArgumentException if no handler is found for the given failure.
     * @throws NullPointerException     if the provided failure is null.
     */
    public ResponseEntity<ApiErrorResponse> handle(Failure failure) {
        notNull(failure, "failure cannot be null");
        return switch (failure) {
            case NotFoundFailure(var message) -> notFound(message);
            case ConflictFailure(var message) -> conflict(message);
            case ValidationFailure(var msg, var fieldViolations) -> unprocessableEntity(map(fieldViolations), msg);
            default -> throw new IllegalArgumentException("No handler found");
        };
    }

    private List<ApiErrorField> map(Collection<FieldViolation> fieldViolations) {
        return fieldViolations.stream()
                .map(f -> new ApiErrorField(f.field(), f.message(), f.rejectedValue()))
                .toList();
    }
}
