package org.grants.harvester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for handling grant search requests.
 * 
 * This controller receives search requests, queries the Grants.gov API,
 * invokes Python scripts for ranking and award prediction, and returns
 * the final enriched list of grant opportunities.
 */
@CrossOrigin(origins = "*") // Allow CORS from any origin
@RestController
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Handles POST requests to /search.
     * 
     * This method:
     * 1. Receives a search request from the client.
     * 2. Queries the Grants.gov API for matching grants.
     * 3. Writes the results and user info to a temporary JSON file.
     * 4. Calls a Python script to rank the grants.
     * 5. Reads the ranked results.
     * 6. Calls another Python script to predict award amounts.
     * 7. Reads the final enriched results and returns them to the client.
     *
     * @param request The search parameters from the client
     * @return List of enriched Grant objects
     * @throws Exception if any step fails
     */
    @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchRequest request) throws Exception {
        logger.info("🔎 Received search request:");
        logger.info("Keyword: {}", request.getKeyword());
        logger.info("Department: {}", request.getDepartment());
        logger.info("Institution Type: {}", request.getInstitutionType());
        logger.info("State: {}", request.getUserState());

        // Step 1: Get initial results from Grants.gov API
        List<Grant> grants = GrantSearcher.run(request);

        // Step 2: Write input JSON for ranking (includes user info and grant results)
        Map<String, Object> jsonWrapper = Map.of(
            "user", Map.of(
                "department", request.getDepartment(),
                "institutionType", request.getInstitutionType(),
                "state", request.getUserState()
            ),
            "results", grants
        );

        Path inputPath = Files.createTempFile("grants_input", ".json");
        Path rankedPath = Files.createTempFile("grants_output", ".json");
        mapper.writeValue(inputPath.toFile(), jsonWrapper);

        // Step 3: Call Python ranking script (rank_grants_cli.py)
        ProcessBuilder pb1 = new ProcessBuilder(
            "python", "rank_grants_cli.py",
            inputPath.toString(),
            rankedPath.toString()
        );
        pb1.directory(new File("C:/Users/gavin/sr25/grant-search")); // Set working directory for script
        pb1.redirectErrorStream(true); // Merge stderr with stdout
        Process rankProcess = pb1.start();
        int rankExit = rankProcess.waitFor();

        // If ranking script fails, log error and return unranked results
        if (rankExit != 0) {
            logger.error("[!] Ranking script failed (exit {}). Returning unranked results.", rankExit);
            return grants;
        }

        // Step 4: Read ranked results from output file
        List<Grant> ranked = mapper.readValue(rankedPath.toFile(), new TypeReference<List<Grant>>() {});

        // Step 5: Write ranked results and user info to file for prediction
        Path predInput = Files.createTempFile("predict_input", ".json");
        Path predOutput = Files.createTempFile("predict_output", ".json");
        Map<String, Object> predWrapper = Map.of(
            "user", Map.of(
                "department", request.getDepartment(),
                "institutionType", request.getInstitutionType(),
                "state", request.getUserState()
            ),
            "results", ranked
        );
        mapper.writeValue(predInput.toFile(), predWrapper);

        // Step 6: Call Python prediction script (predict_awards_cli.py)
        ProcessBuilder pb2 = new ProcessBuilder(
            "python", "predict_awards_cli.py",
            predInput.toString(),
            predOutput.toString()
        );
        pb2.directory(new File(".")); // Use current directory for prediction script
        pb2.redirectErrorStream(true);
        Process predictProcess = pb2.start();
        int predictExit = predictProcess.waitFor();

        // Read and log output from the prediction script
        BufferedReader reader = new BufferedReader(new InputStreamReader(predictProcess.getInputStream()));
        StringBuilder fullOutput = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            logger.info("Python: " + line);
            fullOutput.append(line).append("\n");
        }
        // If prediction script fails, log error and return ranked results
        if (predictExit != 0) {
            logger.error("[!] Award prediction script failed (exit {}). Output:\n{}", predictExit, fullOutput.toString());
            return ranked;
        }

        // Step 7: Read and return the final enriched results
        List<Grant> finalResults = mapper.readValue(predOutput.toFile(), new TypeReference<List<Grant>>() {});
        return finalResults;
    }

}
