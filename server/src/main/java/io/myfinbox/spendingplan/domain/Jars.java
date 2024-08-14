package io.myfinbox.spendingplan.domain;

import io.myfinbox.spendingplan.domain.Plan.PlanIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Jars extends JpaRepository<Jar, JarIdentifier> {

    boolean existsByNameAndPlan(String name, Plan plan);

    @Query(value = """
            SELECT j FROM Jar j
            WHERE j.id = :jarId AND j.plan.id = :planId
            """)
    Optional<Jar> findByIdAndPlanId(JarIdentifier jarId, PlanIdentifier planId);

    @Query(value = """
            SELECT j FROM Jar j
            WHERE j.plan.id = :planId
            """)
    List<Jar> findByPlanId(PlanIdentifier planId);

}
