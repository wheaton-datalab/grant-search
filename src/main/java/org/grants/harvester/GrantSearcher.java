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
 * Utility class for searching grants using the Grants.gov API.
 * 
 * Contains a static method to perform a search based on the provided configuration,
 * send a POST request, parse the response, and return a list of Grant objects.
 */
public class GrantSearcher {

    /**
     * Runs a grant search using the provided configuration.
     *
     * @param config The search configuration parameters
     * @return List of Grant objects matching the search criteria
     * @throws Exception if an error occurs during the HTTP request or parsing
     */
    public static List<Grant> run(SearchConfig config) throws Exception {

         // Grants.gov API endpoint for searching grant opportunities
        String url = "https://api.grants.gov/v1/api/search2";

        // Build request payload dynamically from loaded config file
        Map<String, Object> requestBody = Map.of(
            "keyword", config.keyword,
            "oppStatuses", String.join("|", config.oppStatuses),
            "agencies", String.join("|", config.agencies),
            "fundingCategories", String.join("|", config.fundingCategories),
            "rows", config.rows
        );

        // Convert the request payload to a JSON string
        String json = new ObjectMapper().writeValueAsString(requestBody);

        // Create an HTTP client for sending the POST request
        try (CloseableHttpClient client = HttpClients.createDefault()) {
             //build POST request
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            // Send the POST request and receive the response
            try (CloseableHttpResponse response = client.execute(post)) {
                InputStream content = response.getEntity().getContent();
                String responseBody = new String(content.readAllBytes(), StandardCharsets.UTF_8);

                // Parse the JSON response into a GrantsApiResponse object
                ObjectMapper mapper = new ObjectMapper();
                GrantsApiResponse root = mapper.readValue(responseBody, GrantsApiResponse.class);

                List<Grant> results = root.data.oppHits;

                // Print the top 5 results to the console for quick inspection
                //WILL BE DELETED, JUST FOR TESTING
                for (int i = 0; i < Math.min(results.size(), 5); i++) {
                    Grant g = results.get(i);
                    System.out.println("ID: " + g.id);
                    System.out.println("Title: " + g.title);
                    System.out.println("Number: " + g.number);
                    System.out.println("Agency: " + g.agency + " (" + g.agencyCode + ")");
                    System.out.println("Open Date: " + g.openDate);
                    System.out.println("Close Date: " + g.closeDate);
                    System.out.println("Status: " + g.oppStatus);
                    System.out.println("Doc Type: " + g.docType);
                    System.out.println("CFDA List: " + g.cfdaList);
                    System.out.println("------------------------------------------------");
                }

                return results;
            }
        }
    }
}
