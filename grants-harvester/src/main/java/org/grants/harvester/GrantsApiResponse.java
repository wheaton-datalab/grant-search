package org.grants.harvester;

import java.util.List;

public class GrantsApiResponse {
    public Data data;

    public static class Data {
        public List<Grant> oppHits;
    }
}
