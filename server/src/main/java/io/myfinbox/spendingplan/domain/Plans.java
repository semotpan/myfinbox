package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Plans extends JpaRepository<Plan, PlanIdentifier> {

    @Query(value = """
            SELECT p FROM Plan p
            LEFT JOIN FETCH Jar j
            ON p.id = j.plan.id
            WHERE p.id = :id
            """)
    Optional<Plan> findByIdEagerJars(PlanIdentifier id);

    boolean existsByNameAndAccount(String name, AccountIdentifier accountId);

}
