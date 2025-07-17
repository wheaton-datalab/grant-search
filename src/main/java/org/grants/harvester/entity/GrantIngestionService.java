package org.grants.harvester.service;

import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.grants.harvester.entity.RawGrant;
import org.grants.harvester.repository.RawGrantRepository;

import java.time.Instant;
import java.util.List;

@Service
public class GrantIngestionService {

    private final RawGrantRepository repo;

    public GrantIngestionService(RawGrantRepository repo) {
        this.repo = repo;
    }

    /**
     * Every Monday at 04:00 UTC, ingest new/updated grants.
     * Right now this is a stubbed list; later you’ll call the real grants.gov API.
     */
    @Scheduled(cron = "0 0 4 * * MON")
    @Transactional
    public void ingestWeekly() {
        List<RawGrant> fetched = List.of(
            new RawGrant("OPP-1234", "Climate Research Fellowship",
                         "Deep-dive into climate modelling.", "Posted",
                         "https://grants.gov/opp-1234"),
            new RawGrant("OPP-5678", "Education Innovation Grant",
                         "Pilot new teaching tools.", "Forecasted",
                         "https://grants.gov/opp-5678")
        );

        for (RawGrant g : fetched) {
            repo.findById(g.getOppNo())
                .map(existing -> {
                    if (!existing.getStatus().equals(g.getStatus())) {
                        existing.setStatus(g.getStatus());
                        repo.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> repo.save(g));
        }
    }
}
