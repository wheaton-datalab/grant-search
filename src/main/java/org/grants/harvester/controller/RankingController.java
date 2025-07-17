package org.grants.harvester.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;
import org.grants.harvester.service.RankingService;
import org.grants.harvester.dto.GrantRankDTO;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/{slug}")
    public Page<GrantRankDTO> rankings(
        @PathVariable("slug") String slug,
        @RequestParam(name="page", defaultValue="0") int page,
        @RequestParam(name="size", defaultValue="10") int size
    ) {
        var pageReq = PageRequest.of(page, size);
        return rankingService.getRankings(slug, pageReq);
    }
}
