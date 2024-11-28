package org.dooit;

import com.google.cloud.firestore.annotation.PropertyName;

public class Gig {
    private String gigId;
    private String title;
    private String description;
    private String location;
    private double payRate;
    private String postedBy;
    private boolean available;

    // No-argument constructor required for Firestore serialization
    public Gig() {
    }

    public Gig(String gigId, String title, String description, String location, double payRate, String postedBy) {
        this.gigId = gigId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.payRate = payRate;
        this.postedBy = postedBy;
        this.available = true; // Gigs are available by default
    }

    @PropertyName("gigId")
    public String getGigId() {
        return gigId;
    }

    @PropertyName("gigId")
    public void setGigId(String gigId) {
        this.gigId = gigId;
    }

    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    @PropertyName("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("location")
    public String getLocation() {
        return location;
    }

    @PropertyName("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @PropertyName("payRate")
    public double getPayRate() {
        return payRate;
    }

    @PropertyName("payRate")
    public void setPayRate(double payRate) {
        this.payRate = payRate;
    }

    @PropertyName("postedBy")
    public String getPostedBy() {
        return postedBy;
    }

    @PropertyName("postedBy")
    public void setPostedBy(String postedBy) {
        this.postedBy = postedBy;
    }

    @PropertyName("available")
    public boolean isAvailable() {
        return available;
    }

    @PropertyName("available")
    public void setAvailable(boolean available) {
        this.available = available;
    }
}