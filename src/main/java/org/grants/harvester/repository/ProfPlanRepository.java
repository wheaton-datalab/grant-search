package org.grants.harvester.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.grants.harvester.entity.ProfPlan;
import org.grants.harvester.entity.ProfPlanId;

public interface ProfPlanRepository extends JpaRepository<ProfPlan, ProfPlanId> {
    List<ProfPlan> findByIdProfSlug(String profSlug);
    ProfPlan findByIdProfSlugAndIdOppNo(String profSlug, String oppNo);
    List<ProfPlan> findByIdProfSlugAndIdOppNoIn(String profSlug, List<String> oppNos);
}
