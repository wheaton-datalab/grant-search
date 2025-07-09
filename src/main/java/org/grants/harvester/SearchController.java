package org.grants.harvester;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.web.bind.annotation.*;

import java.io.File;
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
        System.out.println("🔍 Received search request:");
        logger.info("Keyword: {}", request.getKeyword());
        logger.info("Department: {}", request.getDepartment());
        logger.info("Institution Type: {}", request.getInstitutionType());
        logger.info("State: {}", request.getUserState());

        // Step 1: Get original results from Grants.gov API
        List<Grant> grants = GrantSearcher.run(request);

        // Step 2: Prepare JSON payload for Python
        Map<String, Object> jsonWrapper = Map.of(
            "user", Map.of(
                "department", request.getDepartment(),
                "institutionType", request.getInstitutionType(),
                "state", request.getUserState()
            ),
            "results", grants
        );

        // Step 3: Write input JSON to temp file
        Path inputPath = Files.createTempFile("grants_input", ".json");
        Path outputPath = Files.createTempFile("grants_output", ".json");
        mapper.writeValue(inputPath.toFile(), jsonWrapper);

        // Step 4: Run Python script
        ProcessBuilder pb = new ProcessBuilder(
            "python", "rank_grants_cli.py",
            inputPath.toString(),
            outputPath.toString()
        );
        // Replace this with the path to your script
        pb.directory(new File("C:/Users/gavin/sr25/grant-search"));  // CHANGE if needed
        pb.redirectErrorStream(true); // Merge stderr into stdout
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            logger.error("⚠️ Python ranking script failed (exit code {}). Returning unranked results.", exitCode);
            return grants;
        }

        // Step 5: Read ranked results
        List<Grant> ranked = mapper.readValue(outputPath.toFile(), new TypeReference<List<Grant>>() {});
        return ranked;
    }
}
