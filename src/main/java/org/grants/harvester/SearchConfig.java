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
    public String oppStatuses;
    public List<String> agencies;
    public List<String> eligibilities;
    public List<String> fundingCategories;
    public List<String> aln;
    public int rows;

    @Override
    public String toString() {
        return "SearchConfig{" +
                "keyword='" + keyword + '\'' +
                ", oppStatuses='" + oppStatuses + '\'' +
                ", agencies=" + agencies +
                ", eligibilities=" + eligibilities +
                ", fundingCategories=" + fundingCategories +
                ", aln=" + aln +
                ", rows=" + rows +
                '}';
    }
}
