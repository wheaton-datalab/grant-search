package org.grants.harvester;

import java.util.List;

/**
 * POJO representing the search configuration loaded from config.yaml.
 * 
 * This class holds the parameters used to build the Grants.gov API search request.
 */
public class SearchConfig {
    //prominent fields
    public String keyword;
    public List<String> oppStatuses;
    public List<String> agencies;
    public List<String> fundingCategories;
    public int rows;

    @Override
    public String toString() {
        return "SearchConfig{" +
                "keyword='" + keyword + '\'' +
                ", oppStatuses='" + oppStatuses + '\'' +
                ", agencies=" + agencies +
                ", fundingCategories=" + fundingCategories +
                ", rows=" + rows +
                '}';
    }
}
