package org.dooit;

import com.google.cloud.firestore.annotation.PropertyName;

public class Application {
    private String applicationId;
    private String gigId;
    private String username;
    private String status;
    private String reason;     // New field
    private String experience; // New field

    // No-argument constructor required for Firestore
    public Application() {
    }

    // Updated constructor with new fields
    public Application(String applicationId, String gigId, String username, String status, String reason, String experience) {
        this.applicationId = applicationId;
        this.gigId = gigId;
        this.username = username;
        this.status = status;
        this.reason = reason;
        this.experience = experience;
    }

    // Getters and setters...

    @PropertyName("applicationId")
    public String getApplicationId() {
        return applicationId;
    }

    @PropertyName("applicationId")
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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
}