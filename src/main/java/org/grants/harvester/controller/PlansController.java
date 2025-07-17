package org.grants.harvester.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

import org.grants.harvester.dto.GrantPlanDTO;
import org.grants.harvester.service.PipelineService;

/**
 * Exposes two endpoints:
 *  - GET /api/plans/search?name=Prof+Name
 *    → normalizes the free-text name into a slug
 *  - (optional) you can still keep the slug-based one if you like:
 *    GET /api/plans/{slug}
 */
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class PlansController {

    private final PipelineService pipeline;

    public PlansController(PipelineService pipeline) {
        this.pipeline = pipeline;
    }

    @GetMapping(value = "/api/plans/search", produces = "application/json")
    public List<GrantPlanDTO> plansByName(@RequestParam("name") String name) {
        // normalize "Jane Doe" → "jane-doe"
        String slug = name.trim()
                          .toLowerCase()
                          .replaceAll("[^a-z0-9\\s]", "")
                          .replaceAll("\\s+", "-");
        return pipeline.generatePlansForProfessor(slug);
    }
}
