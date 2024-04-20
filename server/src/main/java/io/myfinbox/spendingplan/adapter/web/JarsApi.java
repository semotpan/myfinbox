package io.myfinbox.spendingplan.adapter.web;

import io.myfinbox.rest.CreateJarResource;
import io.myfinbox.rest.JarCategoryModificationResource;
import io.myfinbox.shared.ApiErrorResponse;
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

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public interface JarsApi {

    String TAG = "plans";

    @Operation(summary = "Add a new Jar to an existing spending plan",
            description = "This endpoint adds a new Jar to an existing spending plan in the MyFinBox.",
            security = {@SecurityRequirement(name = "openId")},
            tags = {TAG})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful Operation",
                    headers = @Header(name = LOCATION, description = "Created jar source URI location", schema = @Schema(implementation = URI.class)),
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CreateJarResource.class))),
            @ApiResponse(responseCode = "400", description = "Malformed JSON or Type Mismatch Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Spending plan not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Jar name already exists for the provided spending plan",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Request schema validation failure or provided percentage exceeds available plan percentage",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<?> create(@Parameter(in = PATH, description = "Spending plan ID to be added new jar", required = true) UUID planId,
                             @RequestBody(description = "Jar Resource to be created", required = true) CreateJarResource resource);

    @Operation(
            summary = "Modify Expense Categories for Spending Plan Jar",
            description = "Add or remove provided categories to a spending plan jar in MyFinBox. This endpoint modifies the categories based on the 'toAdd' flag.",
            security = {@SecurityRequirement(name = "openId")},
            tags = {TAG}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success: Categories modified successfully",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Malformed JSON or Type Mismatch Failure",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Spending plan or jar not found",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422",
                    description = "Request schema validation failure. At least one category must be provided, null categories not allowed, or duplicate categories provided",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    ResponseEntity<?> modifyExpenseCategories(
            @Parameter(in = PATH, description = "ID of the spending plan containing the jar to modify categories", required = true) UUID planId,
            @Parameter(in = PATH, description = "ID of the spending jar to modify categories", required = true) UUID jarId,
            @RequestBody(description = "Resource containing categories to add or remove", required = true) JarCategoryModificationResource resource);

}
