package com.ceedpods.crmbuild.enums;

public enum LeadSource {
    MANUAL("Manual Entry"),
    CSV_IMPORT("CSV Import"),
    META_ADS("Meta Ads"),
    GOOGLE_ADS("Google Ads"),
    WHATSAPP("WhatsApp"),
    SALESFORCE("Salesforce"),
    WEBSITE("Website"),
    REFERRAL("Referral"),
    OTHER("Other");

    private final String displayName;

    LeadSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}