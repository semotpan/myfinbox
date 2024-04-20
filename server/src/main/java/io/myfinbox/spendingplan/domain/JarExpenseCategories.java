package io.myfinbox.spendingplan.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import static io.myfinbox.spendingplan.domain.JarExpenseCategory.CategoryIdentifier;

@Repository
public interface JarExpenseCategories extends CrudRepository<JarExpenseCategory, Long> {

    List<JarExpenseCategory> findByJarId(JarIdentifier jarId);

    boolean existsByJarIdAndCategoryId(JarIdentifier jarId, CategoryIdentifier categoryId);

    void deleteByJarIdAndCategoryId(JarIdentifier jarId, CategoryIdentifier categoryId);

}
