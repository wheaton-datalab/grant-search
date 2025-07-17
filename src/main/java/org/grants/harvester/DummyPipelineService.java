package org.grants.harvester;

import org.grants.harvester.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Temporary stub that returns hard-coded data
 * so we can finish wiring the UI before the real pipeline is ready.
 */
// @Service                
public class DummyPipelineService implements PipelineService {

    @Override
    public List<GrantPlanDTO> generatePlansForProfessor(String slug) {

        // 3 sample steps per plan
        PlanDTO p1 = new PlanDTO(
            "Leverage nonlinear dynamics expertise to improve diagnostics.",
            List.of(
                "Highlight bifurcation theory background.",
                "Integrate prior models of epidemics.",
                "Create joint project with control-systems team."
            )
        );

        PlanDTO p2 = new PlanDTO(
            "Use supply-chain modelling work for real-world control tests.",
            List.of(
                "Showcase UAV cargo simulation.",
                "Prototype decision-support dashboard.",
                "Partner with humanitarian org for validation."
            )
        );

        // one grant with two plans
        GrantPlanDTO g = new GrantPlanDTO(
            "Dynamics, Control and Systems Diagnostics",
            9.1,
            "https://example.com/dcsd",
            List.of(p1, p2)
        );

        // return a list (you can add more GrantPlanDTOs later)
        return List.of(g);
    }
}