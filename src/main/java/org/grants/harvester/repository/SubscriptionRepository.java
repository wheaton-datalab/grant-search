package org.grants.harvester.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.grants.harvester.entity.Subscription;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findBySlugAndEnabled(String slug, boolean enabled);
    Subscription findByEmailAndSlug(String email, String slug);
}
