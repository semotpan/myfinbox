package io.myfinbox.expense.adapter.web;

import io.myfinbox.expense.application.CategoryService;
import io.myfinbox.expense.domain.Category;
import io.myfinbox.shared.ApiFailureHandler;
import io.myfinbox.shared.ExpenseCategoryResource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static io.myfinbox.expense.application.CategoryService.CategoryCommand;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@RestController
@RequestMapping(path = "/expenses/category")
@RequiredArgsConstructor
final class ExpenseCategoryController implements ExpenseCategoryControllerApi {

    private final CategoryService categoryService;
    private final ApiFailureHandler apiFailureHandler;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody ExpenseCategoryResource resource) {
        return categoryService.create(new CategoryCommand(resource.getName(), resource.getAccountId()))
                .fold(apiFailureHandler::handle,
                        category -> created(fromCurrentRequest().path("/{id}").build(category.getId().id()))
                                .body(toResource(category)));
    }

    @PutMapping(path = "/{categoryId}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@PathVariable UUID categoryId, @RequestBody ExpenseCategoryResource resource) {
        return categoryService.update(categoryId, new CategoryCommand(resource.getName(), resource.getAccountId()))
                .fold(apiFailureHandler::handle, category -> ok(toResource(category)));
    }

    private ExpenseCategoryResource toResource(Category category) {
        return new ExpenseCategoryResource()
                .categoryId(category.getId().id())
                .accountId(category.getAccount().id())
                .name(category.getName())
                .creationTimestamp(category.getCreationTimestamp().toString());
    }
}
