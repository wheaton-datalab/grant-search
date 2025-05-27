package org.grants.harvester;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;

import java.io.InputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;

public class GrantSearcher {
    public static List<Grant> run(SearchConfig config) throws Exception {

         // Grants.gov API endpoint for searching grant opportunities
        String url = "https://api.grants.gov/v1/api/search2";

        // Build request payload dynamically from loaded config file
        Map<String, Object> requestBody = Map.of(
            "keyword", config.keyword,
            "oppStatuses", config.oppStatuses,
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
                ObjectMapper mapper = new ObjectMapper(); //could fold into below line
                GrantsApiResponse root = mapper.readValue(responseBody, GrantsApiResponse.class);
                return root.data.oppHits;
            }
        }
    }
}
