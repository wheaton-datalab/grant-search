package org.grants.harvester;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * POJO representing the root response from the Grants.gov API.
 * 
 * This class is used for deserializing the JSON response into Java objects.
 * It contains a nested static Data class, which holds the list of Grant objects.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GrantsApiResponse {
     /** The main data object containing the list of grant opportunities */
    public Data data;

    /**
     * Nested static class representing the 'data' section of the API response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        /** List of grant opportunities returned by the API */
        public List<Grant> oppHits;
    }
}
