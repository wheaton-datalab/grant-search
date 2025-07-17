package org.grants.harvester.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String email;

    @Column(nullable = false, length = 128)
    private String slug;

    @Column(nullable = false)
    private boolean enabled;

    protected Subscription() {}

    public Subscription(String email, String slug, boolean enabled) {
        this.email   = email;
        this.slug    = slug;
        this.enabled = enabled;
    }

    // ─── Getters & setters ────────────────────────────────

    public Long getId()       { return id; }
    public String getEmail()  { return email; }
    public String getSlug()   { return slug; }
    public boolean isEnabled(){ return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
