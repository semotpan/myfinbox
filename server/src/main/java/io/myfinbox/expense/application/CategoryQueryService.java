package io.myfinbox.expense.application;

import io.myfinbox.expense.domain.AccountIdentifier;
import io.myfinbox.expense.domain.Categories;
import io.myfinbox.expense.domain.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CategoryQueryService implements CategoryQuery {

    private final Categories categories;

    @Override
    public List<Category> search(UUID accountId) {
        if (isNull(accountId)) {
            return emptyList();
        }

        return categories.findByAccount(new AccountIdentifier(accountId));
    }
}
