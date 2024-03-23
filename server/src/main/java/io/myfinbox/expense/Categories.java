package io.myfinbox.expense;

import io.myfinbox.expense.Category.CategoryIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Categories extends JpaRepository<Category, CategoryIdentifier> {

    List<Category> findByAccount(AccountIdentifier account);

    Optional<Category> findByIdAndAccount(CategoryIdentifier categoryId, AccountIdentifier accountId);

}
