package org.grants.harvester.service;

import java.util.*;
import java.util.stream.Collectors;

import org.grants.harvester.dto.GrantRankDTO;
import org.grants.harvester.entity.ProfPlan;
import org.grants.harvester.repository.ProfPlanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RankingService {

    private final RestTemplate rest;
    private final ProfPlanRepository planRepo;
    private final String matchBaseUrl;

    public RankingService(RestTemplate restTemplate,
                          ProfPlanRepository planRepo,
                          @Value("${grant.match.service.url:http://127.0.0.1:5000}") String baseUrl) {
        this.rest        = restTemplate;
        this.planRepo    = planRepo;
        this.matchBaseUrl = baseUrl;
    }

    public Page<GrantRankDTO> getRankings(String slug, Pageable pageable) {
        int k = 200;
        RankHit[] arr = rest.getForObject(
            matchBaseUrl + "/rank?slug={slug}&k={k}",
            RankHit[].class,
            slug, k
        );
        List<RankHit> hits = (arr == null) ? List.of() : Arrays.asList(arr);

        // active only (defensive)
        hits = hits.stream()
            .filter(h -> {
                String s = h.status == null ? "" : h.status.toLowerCase();
                return "posted".equals(s) || "forecasted".equals(s);
            })
            .toList();

        // which oppNos have plans?
        Set<String> planOpps = planRepo.findByIdProfSlug(slug).stream()
            .map(p -> p.getId().getOppNo())
            .collect(Collectors.toSet());

        List<GrantRankDTO> mapped = hits.stream()
            .map(h -> new GrantRankDTO(
                h.oppNo, h.title, h.status, h.link,
                h.faissScore,
                planOpps.contains(h.oppNo)
            ))
            .toList();

        int start = Math.toIntExact(pageable.getOffset());
        int end   = Math.min(start + pageable.getPageSize(), mapped.size());
        List<GrantRankDTO> slice = (start < end) ? mapped.subList(start, end) : List.of();

        return new PageImpl<>(slice, pageable, mapped.size());
    }

    // matches /rank JSON
    static class RankHit {
        public String oppNo;
        public String title;
        public String status;
        public String link;
        public double faissScore;
    }
}
