package org.grants.harvester;

import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller that handles grant search requests.
 * 
 * Exposes an endpoint for clients to submit search parameters and receive matching grant opportunities.
 */
@CrossOrigin(origins = "*") // Allow requests from any origin (CORS)
@RestController
public class SearchController {
    
    /**
     * Handles POST requests to /search.
     * 
     * Accepts a SearchConfig object in the request body and returns a list of matching Grant objects.
     *
     * @param config The search configuration sent by the client
     * @return List of Grant objects matching the search criteria
     * @throws Exception if an error occurs during the search
     */
    @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchConfig config) throws Exception{
        return GrantSearcher.run(config);
    }

}