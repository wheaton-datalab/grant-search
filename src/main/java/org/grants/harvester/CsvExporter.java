package org.grants.harvester;

import com.opencsv.CSVWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CsvExporter {
    public static void export(List<Grant> grants, String filename) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filename))) {
            writer.writeNext(new String[] {
                "Number", "Title", "Agency", "Open Date", "Close Date", "Status", "CFDA(s)"
            });

            for (Grant g : grants) {
                writer.writeNext(new String[] {
                    g.number,
                    g.title,
                    g.agency,
                    g.openDate,
                    g.closeDate != null ? g.closeDate : "",
                    g.oppStatus,
                    g.cfdaList != null ? String.join(";", g.cfdaList) : ""
                });
            }
        }
    }
}
