package org.grants.harvester;

import java.util.List;

/**
 * Data transfer object representing a search request from the client.
 * 
 * This class encapsulates all user-supplied search parameters for grant opportunities,
 * including keyword, opportunity statuses, funding instruments, and additional metadata
 * such as department, institution type, and user state.
 */
public class SearchRequest {
    private String keyword;
    private List<String> oppStatuses;
    private List<String> fundingInstruments;
    private int rows;

    private String department;
    private String institutionType;
    private String userState;

    // Getters and setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<String> getOppStatuses() { return oppStatuses; }
    public void setOppStatuses(List<String> oppStatuses) { this.oppStatuses = oppStatuses; }

    public List<String> getFundingInstruments() { return fundingInstruments; }
    public void setFundingInstruments(List<String> fundingInstruments) { this.fundingInstruments = fundingInstruments; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public String getDepartment() { return department;}
    public void setDepartment(String department) { this.department = department;}

    public String getInstitutionType() { return institutionType; }
    public void setInstitutionType(String institutionType) { this.institutionType = institutionType; }

    public String getUserState() { return userState; }
    public void setUserState(String userState) { this.userState = userState; }
}
