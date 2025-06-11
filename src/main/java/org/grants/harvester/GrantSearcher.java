package org.grants.harvester;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

/**
 * Utility class for searching and enriching grant opportunities using the Grants.gov API.
 */
public class GrantSearcher {

    // API endpoint for searching grant opportunities
    private static final String SEARCH_API_URL = "https://api.grants.gov/v1/api/search2";
    // API endpoint for fetching detailed information about a specific opportunity
    private static final String FETCH_API_URL = "https://api.grants.gov/v1/api/fetchOpportunity";

    /**
     * Runs a grant search using the provided configuration, then enriches each result with additional details.
     *
     * @param config The search configuration parameters
     * @return List of Grant objects matching the search criteria, enriched with details
     * @throws Exception if an error occurs during the HTTP request or parsing
     */
    public static List<Grant> run(SearchConfig config) throws Exception {

        // Build the request payload for the search API from the config object
        Map<String, Object> requestBody = Map.of(
            "keyword", config.keyword,
            "oppStatuses", String.join("|", config.oppStatuses),
            "fundingInstruments", String.join("|", config.fundingInstruments),
            "rows", config.rows
        );

        // Create an HTTP client for sending requests
        try (CloseableHttpClient client = HttpClients.createDefault()) {

            ObjectMapper mapper = new ObjectMapper();

            // Prepare the POST request for the search API
            HttpPost searchPost = new HttpPost(SEARCH_API_URL);
            searchPost.setHeader("Content-Type", "application/json");
            searchPost.setEntity(new StringEntity(mapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON));

            // Send the search request and process the response
            try (CloseableHttpResponse searchResponse = client.execute(searchPost)) {
                InputStream searchContent = searchResponse.getEntity().getContent();
                String searchResponseBody = new String(searchContent.readAllBytes(), StandardCharsets.UTF_8);

                // Parse the JSON response into a GrantsApiResponse object
                GrantsApiResponse root = mapper.readValue(searchResponseBody, GrantsApiResponse.class);

                List<Grant> grants = root.data.oppHits;

                System.out.println("Received " + grants.size() + " results");

                // STEP 2: Enrich each grant with additional details from the fetchOpportunity API
                for (Grant grant : grants) {
                    try {
                        enrichGrant(grant, client, mapper);
                    } catch (Exception e) {
                        System.err.println("Failed to enrich grant " + grant.id + ": " + e.getMessage());
                    }
                }

                return grants;
            }
        }
    }

    /**
     * Enriches a Grant object with additional details by calling the fetchOpportunity API.
     *
     * @param grant  The Grant object to enrich
     * @param client The HTTP client to use for the request
     * @param mapper The ObjectMapper for JSON parsing
     * @throws Exception if an error occurs during the HTTP request or parsing
     */
    private static void enrichGrant(Grant grant, CloseableHttpClient client, ObjectMapper mapper) throws Exception {

    System.out.println("Calling fetchOpportunity for grant id=" + grant.id);

    if (grant.id == null || grant.id.isBlank()) {
        System.out.println("Skipping grant with blank id");
        return;
    }

    int opportunityId;
    try {
        opportunityId = Integer.parseInt(grant.id);
    } catch (NumberFormatException e) {
        System.out.println("Skipping grant with non-numeric id=" + grant.id);
        return;
    }

    Map<String, Object> fetchRequestBody = Map.of(
        "opportunityId", opportunityId
    );

    HttpPost fetchPost = new HttpPost(FETCH_API_URL);
    fetchPost.setHeader("Content-Type", "application/json");
    fetchPost.setEntity(new StringEntity(mapper.writeValueAsString(fetchRequestBody), ContentType.APPLICATION_JSON));

    try (CloseableHttpResponse fetchResponse = client.execute(fetchPost)) {
        InputStream fetchContent = fetchResponse.getEntity().getContent();
        String fetchResponseBody = new String(fetchContent.readAllBytes(), StandardCharsets.UTF_8);

        System.out.println("fetchOpportunity raw response for id=" + grant.id + ": " + fetchResponseBody);

        Map<?, ?> fetchRoot = mapper.readValue(fetchResponseBody, Map.class);
        Map<?, ?> data = (Map<?, ?>) fetchRoot.get("data");

        if (data != null) {
            Map<?, ?> synopsis = (Map<?, ?>) data.get("synopsis");

            if (synopsis != null) {
                // Set description
                String description = (String) synopsis.get("synopsisDesc");
                grant.description = description != null ? description.trim() : "(No description)";

                // Set award ceiling and floor
                Object ceiling = synopsis.get("awardCeiling");
                Object floor = synopsis.get("awardFloor");

                grant.awardFloor = (floor != null) ? floor.toString() : null;
                grant.awardCeiling = (ceiling != null) ? ceiling.toString() : null;
            } else {
                grant.description = "(No synopsis)";
                grant.awardFloor = null;
                grant.awardCeiling = null;
            }

            // Set grant URL
            grant.url = "https://www.grants.gov/search-results-detail/" + grant.id;

            System.out.println("Enriched grant " + grant.id + ": URL=" + grant.url);
        } else {
            System.out.println("No 'data' field returned for id=" + grant.id);
        }
    }
}
}