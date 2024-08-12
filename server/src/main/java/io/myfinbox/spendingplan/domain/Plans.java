package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Plans extends JpaRepository<Plan, PlanIdentifier> {

    @Query(value = """
            SELECT p FROM Plan p
            LEFT JOIN FETCH p.jars
            WHERE p.id = :id
            """)
    Optional<Plan> findByIdEagerJars(PlanIdentifier id);

    @Query(value = """
            SELECT p FROM Plan p
            LEFT JOIN FETCH p.jars
            WHERE p.id = :id AND p.account = :account
            """)
    Optional<Plan> findByIdAndAccountIdEagerJars(PlanIdentifier id, AccountIdentifier account);

    @Query(value = """
            SELECT p FROM Plan p
            LEFT JOIN FETCH p.jars
            WHERE p.account = :account
            """)
    List<Plan> findByAccountIdEagerJars(AccountIdentifier account);

    boolean existsByNameAndAccount(String name, AccountIdentifier accountId);
}
