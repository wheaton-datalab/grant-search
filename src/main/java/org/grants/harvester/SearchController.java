package org.grants.harvester;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller that handles grant search requests.
 * 
 * Exposes an endpoint for clients to submit search parameters and receive matching grant opportunities.
 */
@CrossOrigin(origins = "*") // Allow requests from any origin (CORS)
@RestController
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    /**
     * Handles POST requests to /search.
     * 
     * Accepts a SearchRequest object in the request body and returns a list of matching Grant objects.
     *
     * @param request The search request sent by the client
     * @return List of Grant objects matching the search criteria
     * @throws Exception if an error occurs during the search
     */
    @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchRequest request) throws Exception {
        // Log request parameters for debugging
        logger.info("🔎 Received search request:");
        logger.info("Keyword: {}", request.getKeyword());
        logger.info("Opportunity Statuses: {}", request.getOppStatuses());
        logger.info("Funding Instruments: {}", request.getFundingInstruments());
        logger.info("Rows: {}", request.getRows());

        // Log new user context fields
        logger.info("User Department: {}", request.getDepartment());
        logger.info("User Institution Type: {}", request.getInstitutionType());
        logger.info("User State: {}", request.getUserState());

        // Pass request to GrantSearcher
        return GrantSearcher.run(request);
    }
}
