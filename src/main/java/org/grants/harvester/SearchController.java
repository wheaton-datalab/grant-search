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
            System.out.println("State: " + request.getState());

            // Step 1: Serialize the Java request to JSON input file
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
                    request.getState()
                );

            Path inputFilePath = Paths.get("input.json");
            Files.writeString(inputFilePath, inputJson);
            System.out.println("✅ Wrote input.json for Python: " + inputFilePath.toAbsolutePath());

            // Step 2: Define the Python script and output file
            String pythonScript = "predict_awards_cli.py";
            String pythonBinary = "python";  // works because of Docker symlink
            String outputFile = "ranked_output.json";

            // Step 3: Construct and start the process
            ProcessBuilder pb = new ProcessBuilder(
                pythonBinary, pythonScript, inputFilePath.toString()
            );
            pb.directory(new File(".")); // explicitly set working directory to /app
            pb.redirectErrorStream(true); // merge stdout and stderr

            System.out.println("🚀 Running Python script...");
            Process process = pb.start();

            // Step 4: Pipe Python output to log for debugging
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

            // Step 5: Read output JSON and return it
            String outputJson = Files.readString(Paths.get(outputFile));
            return ResponseEntity.ok().body(outputJson);

        } catch (Exception e) {
            System.err.println("🔥 ERROR in SearchController: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Server encountered an error: " + e.getMessage());
        }
    }
}
