package org.dooit;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
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
        if (application != null) {
            CollectionReference applications = db.collection(COLLECTION_NAME);
            DocumentReference appDoc = applications.document(application.getApplicationId());
            ApiFuture<WriteResult> result = appDoc.set(application);
            try {
                result.get(); // Ensure the write completes successfully
                // Debug statement removed
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error creating application: " + e.getMessage());
                return false;
            }
        }
        return false; // Invalid application object
    }

    public boolean updateApplication(Application application) {
        if (application != null) {
            DocumentReference appDoc = db.collection(COLLECTION_NAME).document(application.getApplicationId());
            ApiFuture<WriteResult> result = appDoc.set(application);
            try {
                result.get();
                return true;
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error updating application: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public List<Application> getApplicationsByUsername(String username) {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("username", username).get();
        List<Application> applicationList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Application application = document.toObject(Application.class);
                applicationList.add(application);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving applications: " + e.getMessage());
        }
        return applicationList;
    }

    public List<Application> getApplicationsByGigId(String gigId) {
        CollectionReference applications = db.collection(COLLECTION_NAME);
        ApiFuture<QuerySnapshot> query = applications.whereEqualTo("gigId", gigId).get();
        List<Application> applicationList = new ArrayList<>();
        try {
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                Application application = document.toObject(Application.class);
                applicationList.add(application);
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error retrieving applications: " + e.getMessage());
        }
        return applicationList;
    }

    // Method to check if an application ID already exists
    public boolean applicationIdExists(String applicationId) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(applicationId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        try {
            DocumentSnapshot document = future.get();
            return document.exists();
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error checking application ID existence: " + e.getMessage());
            return false;
        }
    }

    // Method to get total number of applications
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

    // Method to get total number of approved applications
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
}