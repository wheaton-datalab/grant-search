package org.grants.harvester;

import java.util.List;

public class SearchConfig {
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
