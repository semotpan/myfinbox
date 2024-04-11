package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Plans extends JpaRepository<Plan, PlanIdentifier> {

    boolean existsByNameAndAccount(String name, AccountIdentifier accountId);

}
