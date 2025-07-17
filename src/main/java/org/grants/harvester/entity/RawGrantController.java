package org.grants.harvester.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
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

    /** List everything in the table */
    @GetMapping
    public List<RawGrant> all() {
        return repo.findAll();
    }

    /** Manually trigger the weekly ingestion logic */
    @PostMapping("/ingest")
    public void ingestNow() {
        ingestion.ingestWeekly();
    }
}
