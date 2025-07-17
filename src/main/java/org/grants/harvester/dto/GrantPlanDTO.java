package org.grants.harvester.dto;

import java.util.List;

public record GrantPlanDTO(
        String title,
        double  fitScore,
        String  link,
        List<PlanDTO> plans
) {}
