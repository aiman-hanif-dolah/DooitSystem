package org.dooit;

import com.google.cloud.firestore.annotation.PropertyName;

public class Application extends BaseEntity {
    private String gigId;
    private String username;
    private String status;
    private String reason;
    private String experience;
    private String contactInfo; // New field for contact information

    public Application() {
        super();
    }

    public Application(String id, String gigId, String username, String status, String reason, String experience, String contactInfo) {
        super(id);
        this.gigId = gigId;
        this.username = username;
        this.status = status;
        this.reason = reason;
        this.experience = experience;
        this.contactInfo = contactInfo;
    }

    @PropertyName("applicationId")
    public String getApplicationId() {
        return getId();
    }

    @PropertyName("applicationId")
    public void setApplicationId(String applicationId) {
        setId(applicationId);
    }

    @PropertyName("gigId")
    public String getGigId() {
        return gigId;
    }

    @PropertyName("gigId")
    public void setGigId(String gigId) {
        this.gigId = gigId;
    }

    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("reason")
    public String getReason() {
        return reason;
    }

    @PropertyName("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @PropertyName("experience")
    public String getExperience() {
        return experience;
    }

    @PropertyName("experience")
    public void setExperience(String experience) {
        this.experience = experience;
    }

    @PropertyName("contactInfo")
    public String getContactInfo() {
        return contactInfo;
    }

    @PropertyName("contactInfo")
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    // Annotate getDetails to map with Firestore's "details" field
    @PropertyName("details")
    public String getDetails() {
        String details = "Gig ID: " + gigId + ", Username: " + username + ", Status: " + status +
                ", Reason: " + reason + ", Experience: " + experience;
        if ("Approved".equalsIgnoreCase(status) && contactInfo != null) {
            details += ", Contact: " + contactInfo;
        }
        return details;
    }

    @PropertyName("details")
    public void setDetails(String details) {
        // Stub setter to satisfy Firestore mapping. No actual implementation needed.
    }
}
