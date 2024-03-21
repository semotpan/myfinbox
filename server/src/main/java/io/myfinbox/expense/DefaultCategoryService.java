package io.myfinbox.expense;

import io.myfinbox.shared.Failure;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Objects.isNull;

@Service
@RequiredArgsConstructor
class DefaultCategoryService implements CategoryService {

    private final Categories categories;

    @Override
    @Transactional
    public Either<Failure, List<Category>> initDefaultCategories(AccountIdentifier account) {
        if (isNull(account)) {
            return Either.left(Failure.ofValidation("AccountIdentifier cannot be null", List.of()));
        }

        var values = DefaultCategories.asList().stream()
                .map(c -> new Category(c, account))
                .toList();

        categories.saveAll(values);

        return Either.right(values);
    }
}
