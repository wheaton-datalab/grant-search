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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;


public class Main {
    public static void main(String[] args) throws Exception {
        String url = "https://api.grants.gov/v1/api/search2";

        // Load config.yaml into SearchConfig
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        SearchConfig config = yamlMapper.readValue(new File("config.yaml"), SearchConfig.class);

        // Build request payload dynamically
        Map<String, Object> requestBody = Map.of(
            "keyword", config.keyword,
            "oppStatuses", config.oppStatuses,
            "agencies", String.join("|", config.agencies),
            "fundingCategories", String.join("|", config.fundingCategories),
            "rows", config.rows
        );

String json = new ObjectMapper().writeValueAsString(requestBody);


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
                for (Grant g : root.data.oppHits) 
                    System.out.println(g);
                
                CsvExporter.export(root.data.oppHits, "grants.csv");
                System.out.println("\nSaved to grants.csv");


            }
        }
    }
}
