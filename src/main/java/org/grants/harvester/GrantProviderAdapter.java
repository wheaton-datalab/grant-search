package org.grants.harvester;

import java.util.List;

/**
 * Interface for grant provider adapters.
 * 
 * Implementations of this interface provide a way to search for grants
 * from different data sources or APIs using a common SearchRequest object.
 */
public interface GrantProviderAdapter {
    /**
     * Searches for grants based on the given search request.
     *
     * @param request The search parameters
     * @return A list of matching Grant objects
     */
    List<Grant> searchGrants(SearchRequest request);
}
