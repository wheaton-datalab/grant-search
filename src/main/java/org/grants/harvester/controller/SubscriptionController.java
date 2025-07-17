package org.grants.harvester.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.grants.harvester.entity.Subscription;
import org.grants.harvester.repository.SubscriptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository repo;

    public SubscriptionController(SubscriptionRepository repo) {
        this.repo = repo;
    }

    /** Create or update a subscription */
    @PostMapping
    public ResponseEntity<Void> upsert(@RequestBody SubscriptionDTO dto) {
        Subscription existing = repo.findByEmailAndSlug(dto.email, dto.slug);
        if (existing != null) {
            existing.setEnabled(dto.enabled);
            repo.save(existing);
        } else {
            repo.save(new Subscription(dto.email, dto.slug, dto.enabled));
        }
        return ResponseEntity.ok().build();
    }

    /** List emails subscribed to a given slug */
    @GetMapping
    public List<String> subscribers(@RequestParam("slug") String slug) {
        return repo.findBySlugAndEnabled(slug, true)
                   .stream()
                   .map(Subscription::getEmail)
                   .collect(Collectors.toList());
    }

    /** DTO for POST body */
    public static class SubscriptionDTO {
        public String email;
        public String slug;
        public boolean enabled;
    }
}
