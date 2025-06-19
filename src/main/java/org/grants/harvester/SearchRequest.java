package org.grants.harvester;

import java.util.List;

public class SearchRequest {
    private String keyword;
    private List<String> oppStatuses;
    private List<String> fundingInstruments;
    private int rows;

    // Getters and setters
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public List<String> getOppStatuses() { return oppStatuses; }
    public void setOppStatuses(List<String> oppStatuses) { this.oppStatuses = oppStatuses; }

    public List<String> getFundingInstruments() { return fundingInstruments; }
    public void setFundingInstruments(List<String> fundingInstruments) { this.fundingInstruments = fundingInstruments; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
}
