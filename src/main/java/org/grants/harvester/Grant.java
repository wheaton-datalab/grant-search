package org.grants.harvester;

import java.util.List;

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
