package org.grants.harvester;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting grant search results to a CSV file.
 * 
 * This class provides a static method to write a list of Grant objects
 * into a CSV file with appropriate headers.
 */
public class CsvExporter {

     /**
     * Exports a list of Grant objects to a CSV file.
     *
     * @param grants   List of Grant objects to export
     * @param filename Name of the CSV file to write to
     * @throws IOException if an I/O error occurs during writing
     */
    public static void export(List<Grant> grants, String filename) throws IOException {
         // Create a CSVWriter with the given filename (overwrites if exists)
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
             // Write the CSV header row
            writer.writeNext(new String[] {
                "Number", "Title", "Agency", "Open Date", "Close Date", "Status", "CFDA(s)"
            });

            // Write each grant's data as a row in the CSV
            for (Grant g : grants) {
                writer.writeNext(new String[] {
                    g.number,
                    g.title,
                    g.agency,
                    g.openDate,
                    g.closeDate != null ? g.closeDate : "",
                    g.oppStatus,
                });
            }
        }
    }
}
