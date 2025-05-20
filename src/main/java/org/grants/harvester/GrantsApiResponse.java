package org.grants.harvester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GrantsApiResponse {
    public Data data;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        public List<Grant> oppHits;
    }
}
