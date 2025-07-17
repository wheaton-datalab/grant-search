package org.grants.harvester;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.util.*;
import java.nio.file.*;

@RestController
public class SearchController {

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        try {
            System.out.println("🔎 Received search request:");
            System.out.println("Keyword: " + request.getKeyword());
            System.out.println("Department: " + request.getDepartment());
            System.out.println("Institution Type: " + request.getInstitutionType());
            System.out.println("State: " + request.getUserState());

            // Step 1: Write input.json to /app directory
            String inputJson = """
                {
                  "keyword": "%s",
                  "department": "%s",
                  "institution_type": "%s",
                  "state": "%s"
                }
                """.formatted(
                    request.getKeyword(),
                    request.getDepartment(),
                    request.getInstitutionType(),
                    request.getUserState()
                );

            Path inputPath = Paths.get("/app/input.json");
            Files.writeString(inputPath, inputJson);
            System.out.println("✅ Wrote input.json: " + inputPath.toAbsolutePath());

            // Step 2: Define paths
            String pythonBinary = "/usr/bin/python3";  // absolute path to Python
            String scriptPath = "/app/predict_awards_cli.py";  // absolute path to script
            String outputPath = "/app/ranked_output.json";

            // Step 3: Run the Python script
            ProcessBuilder pb = new ProcessBuilder(
                pythonBinary, scriptPath, inputPath.toString()
            );
            pb.redirectErrorStream(true);
            pb.directory(new File("/app"));

            System.out.println("🚀 Running Python script at: " + scriptPath);
            Process process = pb.start();

            // Step 4: Pipe Python output for debugging
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                }
            }

            int exitCode = process.waitFor();
            System.out.println("✅ Python script exited with code: " + exitCode);
            if (exitCode != 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Python script failed with exit code " + exitCode);
            }

            // Step 5: Read ranked_output.json
            String outputJson = Files.readString(Paths.get(outputPath));
            return ResponseEntity.ok().body(outputJson);

        } catch (Exception e) {
            System.err.println("🔥 ERROR in SearchController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server error: " + e.getMessage());
        }
    }
}
