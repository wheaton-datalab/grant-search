package org.grants.harvester;

import java.util.List;
import org.grants.harvester.dto.GrantPlanDTO;

public interface PipelineService {
    List<GrantPlanDTO> generatePlansForProfessor(String slug);
}
