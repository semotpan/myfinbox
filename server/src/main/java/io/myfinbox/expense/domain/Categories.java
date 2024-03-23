package io.myfinbox.expense.domain;

import io.myfinbox.expense.domain.Category.CategoryIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Categories extends JpaRepository<Category, CategoryIdentifier> {

    List<Category> findByAccount(AccountIdentifier account);

    Optional<Category> findByIdAndAccount(CategoryIdentifier categoryId, AccountIdentifier accountId);

    boolean existsByNameAndAccount(String name, AccountIdentifier accountId);

}
