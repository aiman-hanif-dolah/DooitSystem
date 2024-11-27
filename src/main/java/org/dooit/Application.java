package org.dooit;

import com.google.cloud.firestore.annotation.PropertyName;

public class Application {
    private String applicationId;
    private String gigId;
    private String username; // Changed from userId to username
    private String status;

    // No-argument constructor required for Firestore
    public Application() {
    }

    public Application(String applicationId, String gigId, String username, String status) {
        this.applicationId = applicationId;
        this.gigId = gigId;
        this.username = username;
        this.status = status;
    }

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

    @Override
    public String toString() {
        return "Application{" +
                "applicationId='" + applicationId + '\'' +
                ", gigId='" + gigId + '\'' +
                ", username='" + username + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}