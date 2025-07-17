package org.grants.harvester.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.grants.harvester.entity.RawGrant;
import org.grants.harvester.repository.RawGrantRepository;
import org.grants.harvester.service.GrantIngestionService;

@RestController
@RequestMapping("/api/raw-grants")
public class RawGrantController {

    private final RawGrantRepository repo;
    private final GrantIngestionService ingestion;

    public RawGrantController(RawGrantRepository repo,
                              GrantIngestionService ingestion) {
        this.repo      = repo;
        this.ingestion = ingestion;
    }

    /** List grants in pages */
    @GetMapping
    public Page<RawGrant> all(Pageable pageable) {
        return repo.findAll(pageable);
    }

    /** Manually trigger ingestion */
    @PostMapping("/ingest")
    public void ingestNow() {
        ingestion.ingestWeekly();
    }
}
