package org.grants.harvester;

import java.util.List;

public interface GrantProviderAdapter {
    List<Grant> searchGrants(SearchRequest request);
}
