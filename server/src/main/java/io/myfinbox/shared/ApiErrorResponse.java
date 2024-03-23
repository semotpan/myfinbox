package io.myfinbox.shared;

import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collection;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public record ApiErrorResponse(Instant timestamp,
                               Integer status,
                               HttpStatus errorCode,
                               String message,
                               Collection<ApiErrorField> errors) {

    @Builder
    public ApiErrorResponse(HttpStatus httpStatus, String message, Collection<ApiErrorField> errors) {
        this(Instant.now(), httpStatus.value(), httpStatus, message, errors);
    }

    public static ResponseEntity<ApiErrorResponse> badRequest(String message) {
        return ResponseEntity.status(BAD_REQUEST)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(BAD_REQUEST)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> forbidden(String message) {
        return ResponseEntity.status(FORBIDDEN)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(FORBIDDEN)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> notFound(String message) {
        return ResponseEntity.status(NOT_FOUND)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(NOT_FOUND)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> methodNotAllowed(HttpHeaders headers, String message) {
        return ResponseEntity.status(METHOD_NOT_ALLOWED)
                .headers(headers)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(METHOD_NOT_ALLOWED)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> notAcceptable(String message) {
        return ResponseEntity.status(NOT_ACCEPTABLE)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(NOT_ACCEPTABLE)
                        .message(message)
                        .build());

    }

    public static ResponseEntity<ApiErrorResponse> conflict(String message) {
        return ResponseEntity.status(CONFLICT)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(CONFLICT)
                        .message(message)
                        .build());

    }

    public static ResponseEntity<ApiErrorResponse> unsupportedMediaType(HttpHeaders headers, String message) {
        return ResponseEntity.status(UNSUPPORTED_MEDIA_TYPE)
                .headers(headers)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(UNSUPPORTED_MEDIA_TYPE)
                        .message(message)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> unprocessableEntity(Collection<ApiErrorField> errors, String message) {
        return ResponseEntity.status(UNPROCESSABLE_ENTITY)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(UNPROCESSABLE_ENTITY)
                        .message(message)
                        .errors(errors)
                        .build());
    }

    public static ResponseEntity<ApiErrorResponse> internalServerError(String message) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .contentType(APPLICATION_JSON)
                .body(ApiErrorResponse.builder()
                        .httpStatus(INTERNAL_SERVER_ERROR)
                        .message(message)
                        .build());
    }

    public record ApiErrorField(String field, String message, Object rejectedValue) {
    }
}
