package org.grants.harvester.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.grants.harvester.entity.RawGrant;

public interface RawGrantRepository extends JpaRepository<RawGrant, String> {
    // no extra methods needed for now
}
