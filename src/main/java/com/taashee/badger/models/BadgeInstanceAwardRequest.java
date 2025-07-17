package com.taashee.badger.models;

public class BadgeInstanceAwardRequest {
    private String recipientIdentifier;
    private String recipientType;
    private String awardType;
    private String narrative;
    private String email;

    public String getRecipientIdentifier() { return recipientIdentifier; }
    public void setRecipientIdentifier(String recipientIdentifier) { this.recipientIdentifier = recipientIdentifier; }

    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }

    public String getAwardType() { return awardType; }
    public void setAwardType(String awardType) { this.awardType = awardType; }

    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
} 