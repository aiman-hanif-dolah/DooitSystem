package org.dooit;

public abstract class BaseEntity {
    private String id;

    // No-argument constructor for Firestore compatibility
    public BaseEntity() {}

    // Constructor with an ID parameter
    public BaseEntity(String id) {
        this.id = id;
    }

    // Getter for the ID field
    public String getId() {
        return id;
    }

    // Setter for the ID field
    public void setId(String id) {
        this.id = id;
    }

    // Abstract method to be implemented by subclasses for display purposes
    public abstract String getDetails();

    @Override
    public String toString() {
        return "ID: " + id;
    }
}
