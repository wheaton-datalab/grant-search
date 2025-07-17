package org.grants.harvester.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "raw_grant")
public class RawGrant {

    @Id
    @Column(name = "opp_no", length = 64)
    private String oppNo;

    @Column(nullable = false)
    private String title;

    @Column(length = 10_000)
    private String description;

    @Column(length = 128)
    private String status;

    @Column(length = 512)
    private String link;

    @Column(name = "last_updated")
    private Instant lastUpdated = Instant.now();

    protected RawGrant() {}

    public RawGrant(String oppNo, String title, String description,
                    String status, String link) {
        this.oppNo       = oppNo;
        this.title       = title;
        this.description = description;
        this.status      = status;
        this.link        = link;
    }

    // ─── Getters & setters ──────────────────────────────────────────────

    public String getOppNo() { return oppNo; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getLink() { return link; }
    public Instant getLastUpdated() { return lastUpdated; }

    public void setStatus(String status) {
        this.status      = status;
        this.lastUpdated = Instant.now();
    }
}
