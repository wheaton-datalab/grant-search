package org.grants.harvester.dto;

public record GrantRankDTO(
    String oppNo,
    String title,
    String status,
    String link,
    double faissScore,
    boolean hasPlan
) {}
