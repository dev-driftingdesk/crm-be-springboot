package com.ceedpods.crmbuild.enums;

public enum LeadStage {
    NEW("New"),
    CONTACTED("Contacted"),
    QUALIFIED("Qualified"),
    PROPOSAL_SENT("Proposal Sent"),
    NEGOTIATION("Negotiation"),
    CLOSED_WON("Closed Won"),
    CLOSED_LOST("Closed Lost");

    private final String displayName;

    LeadStage(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}