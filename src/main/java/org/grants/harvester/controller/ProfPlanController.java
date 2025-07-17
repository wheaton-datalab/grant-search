package org.grants.harvester.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.grants.harvester.dto.PlanDTO;
import org.grants.harvester.entity.ProfPlan;
import org.grants.harvester.repository.ProfPlanRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prof-plans")
public class ProfPlanController {
    private final ProfPlanRepository repo;
    private final ObjectMapper mapper;

    public ProfPlanController(ProfPlanRepository repo, ObjectMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    @GetMapping("/{slug}/{oppNo}")
    public List<PlanDTO> getPlans(@PathVariable String slug, @PathVariable String oppNo) throws Exception {
        ProfPlan row = repo.findByIdProfSlugAndIdOppNo(slug, oppNo);
        if (row == null) return List.of();
        return mapper.readValue(row.getPlanJson(), new TypeReference<>() {});
    }
}
