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
    
    // Required for Jackson deserialization
    public Grant() {
    }

    public Grant(String id, String title, String number, String agency, 
             String openDate, String closeDate, String description,
             String awardFloor, String awardCeiling, String url) {
        this.id = id;
        this.title = title;
        this.number = number;
        this.agency = agency;
        this.openDate = openDate;
        this.closeDate = closeDate;
        this.description = description;
        this.awardFloor = awardFloor;
        this.awardCeiling = awardCeiling;
        this.url = url;
}


    @Override
    public String toString() {
        return number + " | " + title + " | " + agency + " | Open: " + openDate;
    }
}
