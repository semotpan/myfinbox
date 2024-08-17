package io.myfinbox.spendingplan.adapter.web.converters;

import io.myfinbox.rest.JarExpenseCategoryResource;
import io.myfinbox.spendingplan.domain.JarExpenseCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
final class JarExpenseCategoryToResourceConverter implements Converter<JarExpenseCategory, JarExpenseCategoryResource> {

    @Override
    public JarExpenseCategoryResource convert(JarExpenseCategory category) {
        return new JarExpenseCategoryResource()
                .id(category.getId())
                .categoryId(category.getCategoryId().id())
                .categoryName(category.getCategoryName())
                .creationTimestamp(category.getCreationTimestamp().toString());
    }
}
