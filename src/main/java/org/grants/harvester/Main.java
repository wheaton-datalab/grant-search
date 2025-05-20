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

public class Main {
    public static void main(String[] args) throws Exception {
        String url = "https://api.grants.gov/v1/api/search2";

        // build JSON request body
        String json = new ObjectMapper().writeValueAsString(Map.of(
            "rows", 5,
            "keyword", "climate",
            "oppStatuses", "posted"
        ));

        // create HTTP client
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            // send request and receive response
            try (CloseableHttpResponse response = client.execute(post)) {
                InputStream content = response.getEntity().getContent();
                String responseBody = new String(content.readAllBytes(), StandardCharsets.UTF_8);

                // parse JSON response
                ObjectMapper mapper = new ObjectMapper();
                GrantsApiResponse root = mapper.readValue(responseBody, GrantsApiResponse.class);

                // display opportunities
                System.out.println("\nTop Grant Opportunities:");
                for (Grant g : root.data.oppHits) {
                    System.out.println(g);
                }
            }
        }
    }
}
