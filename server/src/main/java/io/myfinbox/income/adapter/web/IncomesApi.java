package io.myfinbox.income.adapter.web;

import io.myfinbox.rest.IncomeResource;
import io.myfinbox.shared.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface IncomesApi {

    String TAG = "incomes";

    @Operation(summary = "Add a new income in the MyFinBox", description = "Add a new income in the MyFinBox",
            security = {@SecurityRequirement(name = "openId")},
            tags = {TAG})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful Operation",
                    headers = @Header(name = LOCATION, description = "Created income URI location", schema = @Schema(implementation = URI.class)),
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = IncomeResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON or Type Mismatch Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Income source for the provided account was not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Request Schema Validation Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<?> create(@RequestBody(description = "Income Resource to be created", required = true) IncomeResource resource);

}
