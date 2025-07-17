package org.grants.harvester.dto;

import java.util.List;

public record PlanDTO(String rationale, List<String> steps) {}