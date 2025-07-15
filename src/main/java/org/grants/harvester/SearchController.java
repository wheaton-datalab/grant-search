package org.grants.harvester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*")
@RestController
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    private static final ObjectMapper mapper = new ObjectMapper();

   @PostMapping("/search")
    public List<Grant> search(@RequestBody SearchRequest request) throws Exception {
        logger.info("🔎 Received search request:");
        logger.info("Keyword: {}", request.getKeyword());
        logger.info("Department: {}", request.getDepartment());
        logger.info("Institution Type: {}", request.getInstitutionType());
        logger.info("State: {}", request.getUserState());

        // Step 1: Get initial results from Grants.gov API
        List<Grant> grants = GrantSearcher.run(request);

        // Step 2: Write input JSON for ranking
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

        // Step 3: Call Python ranking script
        ProcessBuilder pb1 = new ProcessBuilder(
            "python", "tools/rank_grants_cli.py",
            inputPath.toString(),
            rankedPath.toString()
        );
        pb1.directory(new File("C:/Users/gavin/sr25/grant-search")); // Adjust if needed
        pb1.redirectErrorStream(true);
        Process rankProcess = pb1.start();
        int rankExit = rankProcess.waitFor();

        if (rankExit != 0) {
            logger.error("[!] Ranking script failed (exit {}). Returning unranked results.", rankExit);
            return grants;
        }

        // Step 4: Read ranked results
        List<Grant> ranked = mapper.readValue(rankedPath.toFile(), new TypeReference<List<Grant>>() {});

        // Step 5: Write ranked results to file for prediction
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


        // Step 6: Call Python prediction script
        ProcessBuilder pb2 = new ProcessBuilder(
            "python", "tools/predict_awards_cli.py",
            predInput.toString(),
            predOutput.toString()
        );
        pb2.directory(new File("."));
        pb2.redirectErrorStream(true);
        Process predictProcess = pb2.start();
        int predictExit = predictProcess.waitFor();

        //InputStream is = predictProcess.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(predictProcess.getInputStream()));
        StringBuilder fullOutput = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            logger.info("Python: " + line);
            fullOutput.append(line).append("\n");
        }
        if (predictExit != 0) {
            logger.error("[!] Award prediction script failed (exit {}). Output:\n{}", predictExit, fullOutput.toString());
            return ranked;
        }

        // Step 7: Return enriched final results
        List<Grant> finalResults = mapper.readValue(predOutput.toFile(), new TypeReference<List<Grant>>() {});
        return finalResults;
    }

}
