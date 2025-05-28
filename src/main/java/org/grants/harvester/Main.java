/* 
package org.grants.harvester;

import com.fasterxml.jackson.databind.ObjectMapper;
/*
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;


//import java.io.InputStream;
import java.io.File;
//import java.nio.charset.StandardCharsets;
import java.util.List;
//import java.util.Map;

/**
 * Main class for the Grants Harvester application.
 * 
 * This application loads search parameters from a YAML configuration file,
 * sends a POST request to the Grants.gov API, parses the response,
 * prints the top grant opportunities, and exports them to a CSV file.
 
public class Main {
    public static void main(String[] args) throws Exception {

        ObjectMapper mapper = new ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
        SearchConfig config = mapper.readValue(new File("config.yaml"), SearchConfig.class);
       
        List<Grant> results = GrantSearcher.run(config);

        // Display the top grant opportunities in the console
        System.out.println("\nTop Grant Opportunities:");
        for (Grant g : results) 
            System.out.println(g);
                
        // save to CSV    
        CsvExporter.export(results, "grants.csv");
        System.out.println("\nSaved to grants.csv");
            
    } //end of main
} //end of Main class
*/