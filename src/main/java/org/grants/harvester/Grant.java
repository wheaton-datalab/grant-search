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

    public String description;  // Will hold the synopsisDesc from fetchOpportunity
    public String url;          // Will hold the constructed link to the official page

    @Override
    public String toString() {
        return number + " | " + title + " | " + agency + " | Open: " + openDate;
    }
    /*
     @Override
    public String toString() {
        return "Grant{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", title='" + title + '\'' +
                ", agency='" + agency + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
     */
}
