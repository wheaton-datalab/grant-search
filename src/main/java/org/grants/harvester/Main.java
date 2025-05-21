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

/**
 * Main class for the Grants Harvester application.
 * 
 * This application loads search parameters from a YAML configuration file,
 * sends a POST request to the Grants.gov API, parses the response,
 * prints the top grant opportunities, and exports them to a CSV file.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // Grants.gov API endpoint for searching grant opportunities
        String url = "https://api.grants.gov/v1/api/search2";

         // Load search configuration from config.yaml using Jackson YAML mapper
        ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
        SearchConfig config = yamlMapper.readValue(new File("config.yaml"), SearchConfig.class);

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
                ObjectMapper mapper = new ObjectMapper();
                GrantsApiResponse root = mapper.readValue(responseBody, GrantsApiResponse.class);

                // Display the top grant opportunities in the console
                System.out.println("\nTop Grant Opportunities:");
                for (Grant g : root.data.oppHits) 
                    System.out.println(g);
                
                // save to CSV    
                CsvExporter.export(root.data.oppHits, "grants.csv");
                System.out.println("\nSaved to grants.csv");
            }
        }
    } //end of main
} //end of Main class
