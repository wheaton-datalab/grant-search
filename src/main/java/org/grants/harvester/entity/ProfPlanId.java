package org.grants.harvester.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Embeddable
public class ProfPlanId implements Serializable {
    @Column(name = "prof_slug", length = 128)
    private String profSlug;

    @Column(name = "opp_no", length = 64)
    private String oppNo;

    protected ProfPlanId() {}

    public ProfPlanId(String profSlug, String oppNo) {
        this.profSlug = profSlug;
        this.oppNo    = oppNo;
    }

    public String getProfSlug() { return profSlug; }
    public String getOppNo()    { return oppNo; }

    @Override public boolean equals(Object o) { return o instanceof ProfPlanId p && profSlug.equals(p.profSlug) && oppNo.equals(p.oppNo); }
    @Override public int hashCode() { return (profSlug + "|" + oppNo).hashCode(); }
}
