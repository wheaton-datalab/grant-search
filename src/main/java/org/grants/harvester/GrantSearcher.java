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

public class GrantSearcher {

    private static final String SEARCH_API_URL = "https://api.grants.gov/v1/api/search2";
    private static final String FETCH_API_URL = "https://api.grants.gov/v1/api/fetchOpportunity";

    public static List<Grant> run(SearchConfig config) throws Exception {

        Map<String, Object> requestBody = Map.of(
            "keyword", config.keyword,
            "oppStatuses", String.join("|", config.oppStatuses),
            "agencies", String.join("|", config.agencies),
            "fundingCategories", String.join("|", config.fundingCategories),
            "rows", config.rows
        );

        try (CloseableHttpClient client = HttpClients.createDefault()) {

            ObjectMapper mapper = new ObjectMapper();

            HttpPost searchPost = new HttpPost(SEARCH_API_URL);
            searchPost.setHeader("Content-Type", "application/json");
            searchPost.setEntity(new StringEntity(mapper.writeValueAsString(requestBody), ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse searchResponse = client.execute(searchPost)) {
                InputStream searchContent = searchResponse.getEntity().getContent();
                String searchResponseBody = new String(searchContent.readAllBytes(), StandardCharsets.UTF_8);

                GrantsApiResponse root = mapper.readValue(searchResponseBody, GrantsApiResponse.class);

                List<Grant> grants = root.data.oppHits;

                System.out.println("Received " + grants.size() + " results");

                // STEP 2: Enrich each grant
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

    private static void enrichGrant(Grant grant, CloseableHttpClient client, ObjectMapper mapper) throws Exception {

        // === DEBUG: Are we calling enrichGrant? ===
        System.out.println("Calling fetchOpportunity for grant id=" + grant.id);

        // Defensive: skip if ID is null/empty
        if (grant.id == null || grant.id.isBlank()) {
            System.out.println("Skipping grant with blank id");
            return;
        }

        // Convert id to integer (could throw NumberFormatException)
        int opportunityId;
        try {
            opportunityId = Integer.parseInt(grant.id);
        } catch (NumberFormatException e) {
            System.out.println("Skipping grant with non-numeric id=" + grant.id);
            return;
        }

        // Build fetchOpportunity request
        Map<String, Object> fetchRequestBody = Map.of(
                "opportunityId", opportunityId
        );

        HttpPost fetchPost = new HttpPost(FETCH_API_URL);
        fetchPost.setHeader("Content-Type", "application/json");
        fetchPost.setEntity(new StringEntity(mapper.writeValueAsString(fetchRequestBody), ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse fetchResponse = client.execute(fetchPost)) {
            InputStream fetchContent = fetchResponse.getEntity().getContent();
            String fetchResponseBody = new String(fetchContent.readAllBytes(), StandardCharsets.UTF_8);

            // === DEBUG: Show raw response ===
            System.out.println("fetchOpportunity raw response for id=" + grant.id + ": " + fetchResponseBody);

            // Parse response
            Map<?, ?> fetchRoot = mapper.readValue(fetchResponseBody, Map.class);

            Map<?, ?> data = (Map<?, ?>) fetchRoot.get("data");

            if (data != null) {
                Map<?, ?> synopsis = (Map<?, ?>) data.get("synopsis");

                if (synopsis != null) {
                    String description = (String) synopsis.get("synopsisDesc");
                    grant.description = description != null ? description.trim() : "(No description)";
                } else {
                    grant.description = "(No synopsis)";
                }

                // Add grant URL
                grant.url = "https://www.grants.gov/search-results-detail/" + grant.id;

                // === DEBUG: Success ===
                System.out.println("Enriched grant " + grant.id + ": URL=" + grant.url);
            } else {
                System.out.println("No 'data' field returned for id=" + grant.id);
            }
        }
    }
}
