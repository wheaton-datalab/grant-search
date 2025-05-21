package org.grants.harvester;

import java.util.List;

/**
 * POJO representing a single grant opportunity returned by the Grants.gov API.
 * 
 * This class holds the main fields for each grant, used for display and CSV export.
 */
public class Grant {
    public String id;
    public String number;
    public String title;
    public String agencyCode;
    public String agency;
    public String openDate;
    public String closeDate;
    public String oppStatus;
    public String docType;
    public List<String> cfdaList;

    @Override
    public String toString() {
        return number + " | " + title + " | " + agency + " | Open: " + openDate;
    }
}
