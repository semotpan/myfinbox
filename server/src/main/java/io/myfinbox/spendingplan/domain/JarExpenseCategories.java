package io.myfinbox.spendingplan.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JarExpenseCategories extends CrudRepository<JarExpenseCategory, Long> {

    List<JarExpenseCategory> findByJarId(JarIdentifier jarId);

    boolean existsByJarIdAndCategoryId(JarIdentifier jarId, CategoryIdentifier categoryId);

    void deleteByJarIdAndCategoryId(JarIdentifier jarId, CategoryIdentifier categoryId);

    List<JarExpenseCategory> findByCategoryId(CategoryIdentifier categoryId);

}
