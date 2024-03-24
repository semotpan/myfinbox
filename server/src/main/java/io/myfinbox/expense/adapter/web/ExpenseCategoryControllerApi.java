package io.myfinbox.expense.adapter.web;

import io.myfinbox.shared.ApiErrorResponse;
import io.myfinbox.shared.ExpenseCategoryResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;

import static io.myfinbox.expense.adapter.web.ExpenseControllerApi.TAG;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface ExpenseCategoryControllerApi {

    @Operation(summary = "Add a new expense category in the MyFinBox", description = "Add a new expense category in the MyFinBox",
            security = {@SecurityRequirement(name = "openId")},
            tags = {TAG})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful Operation",
                    headers = @Header(name = LOCATION, description = "Created expense category URI location", schema = @Schema(implementation = URI.class)),
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExpenseCategoryResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON or Type Mismatch Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name already exists",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Request Schema Validation Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<?> create(@RequestBody(description = "Expense Category Resource to be created", required = true) ExpenseCategoryResource resource);

    @Operation(summary = "Update an expense category name in the MyFinBox", description = "Update an expense category name in the MyFinBox",
            security = {@SecurityRequirement(name = "openId")},
            tags = {TAG})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful Operation",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExpenseCategoryResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON or Type Mismatch Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Category not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Category name already exists",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Request Schema Validation Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<?> update(@Parameter(description = "CategoryId to be updated", required = true) UUID categoryId,
                             @RequestBody(description = "Expense Category Resource to be updated", required = true) ExpenseCategoryResource resource);
}
