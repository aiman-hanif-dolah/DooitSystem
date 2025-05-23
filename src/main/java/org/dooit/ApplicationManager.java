package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ApplicationManager {
    private static final String COLLECTION_NAME = "applications";
    private Firestore db;

    // Constructor to initialize Firestore
    public ApplicationManager() throws IOException {
        this.db = FirebaseConfig.getFirestore();
    }

    public boolean createApplication(Application application) {
        // Save to Firestore
        DocumentReference appDoc = db.collection(COLLECTION_NAME).document(application.getApplicationId());
        boolean firestoreSuccess;
        try {
            appDoc.set(application).get(); // Ensure the write completes successfully
            System.out.println("Application saved to Firestore successfully.");
            firestoreSuccess = true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Failed to save application to Firestore: " + e.getMessage());
            firestoreSuccess = false;
        }

        // Save to CSV
        saveApplicationToCSV(application);

        // Return true if at least one save operation was successful
        return firestoreSuccess;
    }

    public List<Application> getApplicationsByUsername(String username) {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("username", username).get();
        List<Application> appList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Application app = document.toObject(Application.class);
                appList.add(app);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting applications by username: " + e.getMessage());
        }
        return appList;
    }

    public List<Application> getApplicationsByGigId(String gigId) {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("gigId", gigId).get();
        List<Application> appList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                Application app = document.toObject(Application.class);
                appList.add(app);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting applications by gig ID: " + e.getMessage());
        }
        return appList;
    }

    public boolean updateApplication(Application application) {
        DocumentReference appDoc = db.collection(COLLECTION_NAME).document(application.getApplicationId());
        ApiFuture<WriteResult> future = appDoc.set(application);
        try {
            future.get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error updating application: " + e.getMessage());
            return false;
        }
    }

    public boolean applicationIdExists(String applicationId) {
        DocumentReference appDoc = db.collection(COLLECTION_NAME).document(applicationId);
        ApiFuture<DocumentSnapshot> future = appDoc.get();
        try {
            DocumentSnapshot document = future.get();
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking if application ID exists: " + e.getMessage());
            return false;
        }
    }

    // Method to get the total number of applications
    public int getApplicationCount() {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.get();
        try {
            QuerySnapshot querySnapshot = query.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting application count: " + e.getMessage());
            return 0;
        }
    }

    // Method to get the number of approved applications
    public int getApprovedApplicationCount() {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("status", "Approved").get();
        try {
            QuerySnapshot querySnapshot = query.get();
            return querySnapshot.size();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error getting approved application count: " + e.getMessage());
            return 0;
        }
    }

    private void saveApplicationToCSV(Application application) {
        String filePath = "applications.csv";

        try (FileWriter writer = new FileWriter(filePath, true)) {
            // Write header if the file is empty
            if (new java.io.File(filePath).length() == 0) {
                writer.write("Application ID,Gig ID,Username,Status,Reason,Experience\n");
            }

            // Write application details
            writer.write(String.format(
                    "\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    application.getApplicationId(),
                    application.getGigId(),
                    application.getUsername(),
                    application.getStatus(),
                    application.getReason().replaceAll("\"", "'"), // Escape quotes
                    application.getExperience().replaceAll("\"", "'") // Escape quotes
            ));
            System.out.println("Application details saved to CSV successfully.");
        } catch (IOException e) {
            System.err.println("Error saving application to CSV: " + e.getMessage());
        }
    }

}