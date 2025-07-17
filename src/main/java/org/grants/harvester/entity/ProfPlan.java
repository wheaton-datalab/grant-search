package org.grants.harvester.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "prof_plan")
public class ProfPlan {

    @EmbeddedId
    private ProfPlanId id;

    @Lob
    @Column(name = "plan_json", nullable = false)
    private String planJson; // full JSON array of plans

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ProfPlan() {}

    public ProfPlan(String profSlug, String oppNo, String planJson) {
        this.id = new ProfPlanId(profSlug, oppNo);
        this.planJson = planJson;
    }

    public ProfPlanId getId()      { return id; }
    public String    getPlanJson() { return planJson; }
    public Instant   getCreatedAt(){ return createdAt; }

    public void setPlanJson(String planJson) { this.planJson = planJson; }
}
