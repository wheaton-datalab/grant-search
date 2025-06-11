package org.grants.harvester;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO representing a single grant opportunity returned by the Grants.gov API.
 * 
 * This class holds the main fields for each grant, used for display and CSV export.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

    public String awardCeiling, awardFloor;
    public String description;
    public String url;         

    @Override
    public String toString() {
        return number + " | " + title + " | " + agency + " | Open: " + openDate;
    }
}
